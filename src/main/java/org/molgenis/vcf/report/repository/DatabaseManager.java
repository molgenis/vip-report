package org.molgenis.vcf.report.repository;

import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.AsciiLineReaderIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import lombok.NonNull;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.repository.SqlUtils.insertLookupValues;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

@Component
public class DatabaseManager {
    public static final String SAMPLE_INDEX = "sampleIndex";
    Connection conn;

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
    public static final String VARIANT_ID = "variantId";

    public DatabaseManager(VcfRepository vcfRepo, InfoRepository infoRepo, NestedRepository nestedRepo, FormatRepository formatRepo, PhenotypeRepository phenotypeRepo, MetadataRepository metadataRepo, ConfigRepository configRepo, DecisionTreeRepository decisionTreeRepo, SampleRepository sampleRepo, ReportMetadataRepository reportMetadataRepo) {
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

    //For unit testing
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

    public Bytes populateDb(String databaseLocation, FieldMetadatas fieldMetadatas, Items<Sample> samples, File vcfFile,
                            Path decisionTreePath, Path sampleTreePath, ReportMetadata reportMetadata, Map<?, ?> templateConfig,
                            @NonNull List<Phenopacket> phenopackets) throws IOException {
        getConnection(databaseLocation);
        boolean isGz = vcfFile.getName().toLowerCase().endsWith(".gz");
        try (InputStream fis = isGz ?
                new GZIPInputStream(new FileInputStream(vcfFile)) :
                new FileInputStream(vcfFile);
             AsciiLineReader reader = new AsciiLineReader(fis);
             AsciiLineReaderIterator vcfIterator = new AsciiLineReaderIterator(reader);) {
            VCFCodec codec = new VCFCodec();
            VCFHeader header = (VCFHeader) codec.readActualHeader(vcfIterator);
                conn.setAutoCommit(false);

                Map<String, FieldMetadata> parentFields = getNestedFields(fieldMetadatas, INFO);
                Map<String, List<String>> nestedFields = new HashMap<>();
                for (String field : parentFields.keySet()) {
                    nestedFields.put(field, getDatabaseNestedColumns(field));
                }
                List<String> lines = new ArrayList<>(header.getMetaDataInInputOrder().stream().map(VCFHeaderLine::toString).toList());
                metadataRepo.insertHeaderLine(conn, lines, getHeaderLine(samples));
                Map<FieldType, Map<String, Integer>> metadataKeys = metadataRepo.insertMetadata(conn, fieldMetadatas, decisionTreePath, sampleTreePath, phenopackets);
                sampleRepo.insertSamples(conn, samples);
                insertVariants(fieldMetadatas, samples, decisionTreePath, sampleTreePath, header, vcfIterator, nestedFields, codec, metadataKeys);
                phenotypeRepo.insertPhenotypeData(conn, phenopackets, samples.getItems());
                configRepo.insertConfigData(conn, templateConfig);
                decisionTreeRepo.insertDecisionTreeData(conn, decisionTreePath, sampleTreePath);
                reportMetadataRepo.insertReportMetadata(conn, reportMetadata);

                conn.commit();
        }catch (SQLException e){
            throw new DatabaseException(e.getMessage(), "populate db");
        }
        byte[] fileContent = Files.readAllBytes(Path.of(databaseLocation));
        return new Bytes(fileContent);
    }

    private void insertVariants(FieldMetadatas fieldMetadatas, Items<Sample> samples, Path decisionTreePath,
                                Path sampleTreePath, VCFHeader header, AsciiLineReaderIterator vcfIterator,
                                Map<String, List<String>> nestedFields, VCFCodec vcfCodec, Map<FieldType, Map<String, Integer>> metadataKeys) throws DatabaseException {
        List<String> formatColumns = getDatabaseFormatColumns();
        List<String> infoColumns = getDatabaseInfoColumns();

        Map<Object, Integer> contigIds = SqlUtils.insertLookupValues(conn, "contig", header.getSequenceDictionary().getSequences().stream().map(SAMSequenceRecord::getSequenceName).toList());
        Map<String, Integer> formatLookup = new HashMap<>();
        String line;
        int formatId = 0;

        while (vcfIterator.hasNext()) {
            line = vcfIterator.next();
            if (!line.startsWith("#")) {
                String[] split = line.split("\t");
                String formatString = split.length >= 9 ? split[8] : null;
                Integer formatValue = null;
                if(formatString != null) {
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
                int variantId = vcfRepo.insertVariant(conn, vc, contigIds, formatValue);

                String infoField = split[7];
                String[] infoItems = infoField.split(";");

                infoRepo.insertInfoFieldOrder(conn, metadataKeys, infoItems, variantId);
                for (Map.Entry<String, List<String>> entry : nestedFields.entrySet()) {
                    nestedRepo.insertNested(conn, entry.getKey(), vc, entry.getValue(), fieldMetadatas, variantId, decisionTreePath != null);
                }
                formatRepo.insertFormatData(conn, vc, formatColumns, variantId, fieldMetadatas, samples.getItems(), sampleTreePath != null);
                infoRepo.insertInfoData(conn, vc, infoColumns, fieldMetadatas, variantId, sampleTreePath != null);
            }
        }
    }



    private void insertFormatValue(int key, String value) {
        String sql = "INSERT INTO formatLookup (id, value) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setString(2, value);
            ps.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), "insert lookup value");
        }
    }

    private Map<String, FieldMetadata> getNestedFields(FieldMetadatas fieldMetadatas, FieldType fieldType) {
        if (fieldType != INFO) {
            throw new UnsupportedOperationException("Nested fields are only supported for INFO fields.");
        }
        return fieldMetadatas.getInfo().entrySet().stream()
                .filter(e -> e.getValue().getNestedFields() != null && !e.getValue().getNestedFields().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String getHeaderLine(Items<Sample> samples) {
        String fixedCols = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT";
        String headerLine;
        if (samples.getItems().isEmpty()) {
            headerLine = fixedCols;
        } else {
            headerLine = fixedCols + "\t" + String.join("\t", samples.getItems().stream().sorted(Comparator.comparingInt(Sample::getIndex))
                    .map(sample -> sample.getPerson().getIndividualId()).toList());
        }
        return headerLine;
    }

    private List<String> getDatabaseInfoColumns() {
        try {
            return getTableColumns("info", c -> !c.equalsIgnoreCase("id") && !c.equalsIgnoreCase(VARIANT_ID));
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), "get info columns");
        }
    }

    private List<String> getDatabaseFormatColumns() {
        try {
            return getTableColumns("format", c -> !c.equalsIgnoreCase("id")
                    && !c.equalsIgnoreCase(SAMPLE_INDEX)
                    && !c.equalsIgnoreCase(VARIANT_ID));
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), "get format columns");
        }
    }

    private List<String> getDatabaseNestedColumns(String field) {
        try {
            return getTableColumns(String.format("variant_%s", field),
                    c -> !c.equalsIgnoreCase("id") && !c.equalsIgnoreCase(VARIANT_ID));
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), "get nested columns");
        }
    }

    private List<String> getTableColumns(String table, java.util.function.Predicate<String> include) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
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
