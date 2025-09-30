package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import lombok.NonNull;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

@Component
public class DatabaseManager {
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
    public static final String VARIANT_ID = "variant_id";

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
                throw new DatabaseException(e.getMessage());
            }
        }
        return this.conn;
    }

    public Bytes populateDb(String databaseLocation, FieldMetadatas fieldMetadatas, Items<Sample> samples, File vcfFile,
                            Path decisionTreePath, Path sampleTreePath, ReportMetadata reportMetadata, Map<?, ?> templateConfig,
                            @NonNull List<Phenopacket> phenopackets) throws IOException {
        getConnection(databaseLocation);
        try (VCFFileReader reader = new VCFFileReader(vcfFile, false)) {
            try {
                conn.setAutoCommit(false);

                VCFHeader header = reader.getFileHeader();

                Map<String, FieldMetadata> parentFields = getNestedFields(fieldMetadatas, INFO);
                Map<String, List<String>> nestedFields = new HashMap<>();
                for (String field : parentFields.keySet()) {
                    nestedFields.put(field, getDatabaseNestedColumns(field));
                }
                List<String> formatColumns = getDatabaseFormatColumns();
                List<String> infoColumns = getDatabaseInfoColumns();

                List<String> lines = new ArrayList<>(header.getMetaDataInInputOrder().stream().map(VCFHeaderLine::toString).toList());
                metadataRepo.insertHeaderLine(conn, lines, getHeaderLine(samples));
                metadataRepo.insertMetadata(conn, fieldMetadatas, decisionTreePath, sampleTreePath, phenopackets);
                for (VariantContext vc : reader) {
                    int variantId = vcfRepo.insertVariant(conn, vc);
                    for (Map.Entry<String, List<String>> entry : nestedFields.entrySet()) {
                        nestedRepo.insertNested(conn, entry.getKey(), vc, entry.getValue(), fieldMetadatas, variantId);
                    }
                    formatRepo.insertFormatData(conn, vc, formatColumns, variantId, fieldMetadatas, samples.getItems());
                    infoRepo.insertInfoData(conn, vc, infoColumns, fieldMetadatas, variantId);
                }
                sampleRepo.insertSamples(conn, samples);
                phenotypeRepo.insertPhenotypeData(conn, phenopackets, samples.getItems());
                configRepo.insertConfigData(conn, templateConfig);
                decisionTreeRepo.insertDecisionTreeData(conn, decisionTreePath, sampleTreePath);
                reportMetadataRepo.insertReportMetadata(conn, reportMetadata);

                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        byte[] fileContent = Files.readAllBytes(Path.of(databaseLocation));
        return new Bytes(fileContent);
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
            headerLine = fixedCols + "\t" +
                    String.join("\t", samples.getItems().stream()
                            .map(sample -> sample.getPerson().getIndividualId()).toList());
        }
        return headerLine;
    }

    private List<String> getDatabaseInfoColumns() throws SQLException {
        return getTableColumns("info", c -> !c.equalsIgnoreCase("id") && !c.equalsIgnoreCase(VARIANT_ID));
    }

    private List<String> getDatabaseFormatColumns() throws SQLException {
        return getTableColumns("format", c -> !c.equalsIgnoreCase("id")
                && !c.equalsIgnoreCase("sample_id")
                && !c.equalsIgnoreCase(VARIANT_ID));
    }

    private List<String> getDatabaseNestedColumns(String field) throws SQLException {
        return getTableColumns(String.format("variant_%s", field),
                c -> !c.equalsIgnoreCase("id") && !c.equalsIgnoreCase(VARIANT_ID));
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
