package org.molgenis.vcf.report.repository;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.repository.SqlUtils.insertLookupValues;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.jspecify.annotations.Nullable;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.HpoTerm;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.report.utils.Utf8LineReader;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class DatabaseManager {
  public static final String SAMPLE_INDEX = "_sampleIndex";
  @Nullable Connection conn;

  private final VcfRepository vcfRepo;
  private final InfoRepository infoRepo;
  private final NestedRepository nestedRepo;
  private final FormatRepository formatRepo;
  private final PhenotypeRepository phenotypeRepo;
  private final MetadataRepository metadataRepo;
  private final ConfigRepository configRepo;
  private final DecisionTreeRepository decisionTreeRepo;
  private final SampleRepository sampleRepo;
  private final ReportMetadataRepository reportMetadataRepo;
  public static final String VARIANT_ID = "_variantId";

  public DatabaseManager(
      VcfRepository vcfRepo,
      InfoRepository infoRepo,
      NestedRepository nestedRepo,
      FormatRepository formatRepo,
      PhenotypeRepository phenotypeRepo,
      MetadataRepository metadataRepo,
      ConfigRepository configRepo,
      DecisionTreeRepository decisionTreeRepo,
      SampleRepository sampleRepo,
      ReportMetadataRepository reportMetadataRepo) {
    this.vcfRepo = vcfRepo;
    this.infoRepo = infoRepo;
    this.nestedRepo = nestedRepo;
    this.formatRepo = formatRepo;
    this.phenotypeRepo = phenotypeRepo;
    this.metadataRepo = metadataRepo;
    this.configRepo = configRepo;
    this.decisionTreeRepo = decisionTreeRepo;
    this.sampleRepo = sampleRepo;
    this.reportMetadataRepo = reportMetadataRepo;
  }

  // For unit testing
  void setConnection(Connection connection) {
    this.conn = requireNonNull(connection);
  }

  public Connection getConnection(String databaseLocation) {
    String databaseUrl = String.format("jdbc:sqlite:%s", databaseLocation);
    if (this.conn == null) {
      try {
        this.conn = DriverManager.getConnection(databaseUrl);
      } catch (SQLException e) {
        throw new DatabaseException(e.getMessage(), "get connection");
      }
    }
    return this.conn;
  }

  private Connection getConnection() {
    if (conn == null) {
      throw new IllegalArgumentException();
    }
    return conn;
  }

  public Bytes populateDb(
      String databaseLocation,
      FieldMetadatas fieldMetadatas,
      Items<Sample> samples,
      File vcfFile,
      @Nullable Path decisionTreePath,
      @Nullable Path sampleTreePath,
      ReportMetadata reportMetadata,
      @Nullable Map<?, ?> templateConfig,
      List<Phenopacket> phenopackets,
      Map<String, HpoTerm> hpoTerms)
      throws IOException {
    getConnection(databaseLocation);
    boolean isGz = vcfFile.getName().toLowerCase(Locale.ROOT).endsWith(".gz");
    try (InputStream fis =
            isGz
                ? new GZIPInputStream(new FileInputStream(vcfFile))
                : new FileInputStream(vcfFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        Utf8LineReader utf8LineReader = new Utf8LineReader(br);
        LineIteratorImpl vcfIterator = new LineIteratorImpl(utf8LineReader)) {
      VCFCodec codec = new VCFCodec();
      VCFHeader header = (VCFHeader) codec.readActualHeader(vcfIterator);
      getConnection().setAutoCommit(false);

      Map<String, FieldMetadata> parentFields = getNestedFields(fieldMetadatas, INFO);
      Map<String, List<String>> nestedFields = new HashMap<>();
      for (String field : parentFields.keySet()) {
        nestedFields.put(field, getDatabaseNestedColumns(field));
      }
      List<String> lines =
          new ArrayList<>(
              header.getMetaDataInInputOrder().stream().map(VCFHeaderLine::toString).toList());
      metadataRepo.insertHeaderLine(getConnection(), lines, getHeaderLine(samples));
      Map<FieldType, Map<String, Integer>> metadataKeys =
          metadataRepo.insertMetadata(
              getConnection(),
              fieldMetadatas,
              decisionTreePath,
              sampleTreePath,
              phenopackets,
              hpoTerms);
      sampleRepo.insertSamples(getConnection(), samples);
      insertVariants(
          fieldMetadatas,
          samples,
          decisionTreePath,
          sampleTreePath,
          header,
          vcfIterator,
          nestedFields,
          codec,
          metadataKeys);
      phenotypeRepo.insertPhenotypeData(getConnection(), phenopackets, samples.getItems());
      if (templateConfig != null) {
        configRepo.insertConfigData(getConnection(), templateConfig);
      }
      decisionTreeRepo.insertDecisionTreeData(getConnection(), decisionTreePath, sampleTreePath);
      reportMetadataRepo.insertReportMetadata(getConnection(), reportMetadata);

      getConnection().commit();
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "populate db");
    }
    byte[] fileContent = Files.readAllBytes(Path.of(databaseLocation));
    return new Bytes(fileContent);
  }

  private void insertVariants(
      FieldMetadatas fieldMetadatas,
      Items<Sample> samples,
      @Nullable Path decisionTreePath,
      @Nullable Path sampleTreePath,
      VCFHeader header,
      LineIteratorImpl vcfIterator,
      Map<String, List<String>> nestedFields,
      VCFCodec vcfCodec,
      Map<FieldType, Map<String, Integer>> metadataKeys)
      throws DatabaseException {
    List<String> formatColumns = getDatabaseFormatColumns();
    List<String> infoColumns = getDatabaseInfoColumns();

    Map<Object, Integer> contigIds =
        SqlUtils.insertLookupValues(
            getConnection(),
            "contig",
            header.getSequenceDictionary() != null
                ? header.getSequenceDictionary().getSequences().stream()
                    .map(SAMSequenceRecord::getSequenceName)
                    .toList()
                : emptyList());
    Map<Object, Integer> gtIds =
        insertLookupValues(getConnection(), "gtType", List.of(GenotypeType.values()));
    Map<String, Integer> formatLookup = new HashMap<>();
    String line;
    int formatId = 0;

    while (vcfIterator.hasNext()) {
      line = vcfIterator.next();
      if (!line.startsWith("#")) {
        String[] split = line.split("\t", -1);
        String formatString = split.length >= 9 ? split[8] : null;
        Integer formatValue = null;
        if (formatString != null) {
          if (formatLookup.containsKey(formatString)) {
            formatValue = formatLookup.get(formatString);
          } else {
            formatLookup.put(formatString, formatId);
            insertFormatValue(formatId, formatString);
            formatValue = formatId;
            formatId++;
          }
        }
        VariantContext vc = vcfCodec.decode(line);
        int variantId = vcfRepo.insertVariant(getConnection(), vc, contigIds, formatValue);

        String infoField = split[7];
        String[] infoItems = infoField.split(";", -1);

        infoRepo.insertInfoFieldOrder(getConnection(), metadataKeys, infoItems, variantId);
        for (Map.Entry<String, List<String>> entry : nestedFields.entrySet()) {
          nestedRepo.insertNested(
              getConnection(),
              entry.getKey(),
              vc,
              entry.getValue(),
              fieldMetadatas,
              variantId,
              decisionTreePath != null);
        }
        formatRepo.insertFormatData(
            getConnection(),
            vc,
            formatColumns,
            variantId,
            fieldMetadatas,
            samples.getItems(),
            sampleTreePath != null,
            gtIds);
        infoRepo.insertInfoData(
            getConnection(), vc, infoColumns, fieldMetadatas, variantId, sampleTreePath != null);
      }
    }
  }

  private void insertFormatValue(int key, String value) {
    String sql = "INSERT INTO formatLookup (id, value) VALUES (?, ?)";
    try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
      ps.setInt(1, key);
      ps.setString(2, value);
      ps.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert lookup value");
    }
  }

  private Map<String, FieldMetadata> getNestedFields(
      FieldMetadatas fieldMetadatas, FieldType fieldType) {
    if (fieldType != INFO) {
      throw new UnsupportedOperationException("Nested fields are only supported for INFO fields.");
    }
    return fieldMetadatas.getInfo().entrySet().stream()
        .filter(
            e ->
                e.getValue().getNestedFields() != null && !e.getValue().getNestedFields().isEmpty())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static String getHeaderLine(Items<Sample> samples) {
    String fixedCols = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT";
    String headerLine;
    if (samples.getItems().isEmpty()) {
      headerLine = fixedCols;
    } else {
      headerLine =
          fixedCols
              + "\t"
              + String.join(
                  "\t",
                  samples.getItems().stream()
                      .sorted(Comparator.comparingInt(Sample::getIndex))
                      .map(sample -> sample.getPerson().getIndividualId())
                      .toList());
    }
    return headerLine;
  }

  private List<String> getDatabaseInfoColumns() {
    try {
      return getTableColumns(
          "info", c -> !c.equalsIgnoreCase("_id") && !c.equalsIgnoreCase(VARIANT_ID));
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "get info columns");
    }
  }

  private List<String> getDatabaseFormatColumns() {
    try {
      return getTableColumns(
          "format",
          c ->
              !c.equalsIgnoreCase("_id")
                  && !c.equalsIgnoreCase(SAMPLE_INDEX)
                  && !c.equalsIgnoreCase(VARIANT_ID));
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "get format columns");
    }
  }

  private List<String> getDatabaseNestedColumns(String field) {
    try {
      return getTableColumns(
          String.format("variant_%s", field),
          c -> !c.equalsIgnoreCase("_id") && !c.equalsIgnoreCase(VARIANT_ID));
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "get nested columns");
    }
  }

  private List<String> getTableColumns(String table, java.util.function.Predicate<String> include)
      throws SQLException {
    List<String> columns = new ArrayList<>();
    try (Statement stmt = getConnection().createStatement()) {
      ResultSet rs = stmt.executeQuery(String.format("PRAGMA table_info(%s)", table));
      while (rs.next()) {
        String column = rs.getString("name");
        if (include.test(column)) {
          columns.add(column);
        }
      }
    }
    return columns;
  }
}
