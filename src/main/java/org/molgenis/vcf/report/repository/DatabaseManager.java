package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

public class DatabaseManager {
    private Connection conn;
    private static final String FILE = "vip-report.db";
    private static final String DB_URL = String.format("jdbc:sqlite:%s", FILE);
    public static final String VARIANT_ID = "variant_id";

    public DatabaseManager() {
        // Delete the existing file
        try {
            Files.deleteIfExists(Path.of(FILE));
            Files.deleteIfExists(Path.of(FILE + ".blob"));//FIXME
            initConnection();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public void createTable(String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace(); //FIXME
        }
    }

    private void initConnection() {
        if (conn == null) {
            try {
                this.conn = DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {
                throw new RuntimeException(e); //FIXME
            }
        }
    }

    public void populateDb(FieldMetadatas fieldMetadatas, Items<Sample> samples, File vcfFile, Path decisionTreePath, Path sampleTreePath, ReportData reportData, ReportMetadata reportMetadata, Map<?, ?> templateConfig) {
        try (
                VCFFileReader reader = new VCFFileReader(vcfFile, false);
        ) {
            try {
                conn.setAutoCommit(false);

                VcfRepository vcfRepo = new VcfRepository(conn);
                PhenotypeRepository phenotypeRepo = new PhenotypeRepository(conn);
                MetadataRepository metadataRepo = new MetadataRepository(conn);
                ConfigRepository configRepo = new ConfigRepository(conn);
                DecisionTreeRepository decisionTreeRepo = new DecisionTreeRepository(conn);
                SampleRepository sampleRepo = new SampleRepository(conn);
                ReportMetadataRepository reportMetadataRepo = new ReportMetadataRepository(conn);
                VCFHeader header = reader.getFileHeader();


                Map<String, FieldMetadata> parentFields = getNestedFields(fieldMetadatas, INFO);
                Map<String,List<String>> nestedFields = new HashMap<>();
                for (String field : parentFields.keySet()) {
                    nestedFields.put(field, getDatabaseNestedColumns(field));
                }
                List<String> formatColumns = getDatabaseFormatColumns();
                List<String> infoColumns = getDatabaseInfoColumns();

                List<String> lines = new ArrayList<>(header.getMetaDataInInputOrder().stream().map(VCFHeaderLine::toString).toList());
                metadataRepo.insertHeaderLine(lines, getHeaderLine(samples));
                metadataRepo.insertMetadata(fieldMetadatas);
                for (VariantContext vc : reader) {
                    int variantId = vcfRepo.insertVariant(vc);
                    for (Map.Entry<String, List<String>> entry : nestedFields.entrySet()) {
                        vcfRepo.insertNested(entry.getKey(), vc, entry.getValue(), fieldMetadatas, variantId);
                    }
                    vcfRepo.insertFormatData(vc, formatColumns, variantId, fieldMetadatas, samples.getItems());
                    vcfRepo.insertInfoData(vc, infoColumns, fieldMetadatas, variantId);
                }
                sampleRepo.insertSamples(samples);
                phenotypeRepo.insertPhenotypeData(reportData.getPhenopackets(), samples.getItems());
                configRepo.insertConfigData(templateConfig);
                decisionTreeRepo.insertDecisionTreeData(decisionTreePath, sampleTreePath);
                reportMetadataRepo.insertReportMetadata(reportMetadata);

                conn.commit();
                Files.copy(Path.of(FILE), Path.of(FILE + ".blob")); //FIXME
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, FieldMetadata> getNestedFields(FieldMetadatas fieldMetadatas, FieldType fieldType) {
        if (fieldType != INFO) {
            throw new UnsupportedOperationException("Nested fields are only supported for INFO fields.");
        }
        return fieldMetadatas.getInfo().entrySet().stream()
                .filter(e -> e.getValue().getNestedFields() != null && !e.getValue().getNestedFields().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
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
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(info)");
            while (rs.next()) {
                String column = rs.getString("name");
                if (!column.equalsIgnoreCase("id") &&
                        !column.equalsIgnoreCase(VARIANT_ID)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    private List<String> getDatabaseNestedColumns(String field) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("PRAGMA table_info(variant_%s)", field));
            while (rs.next()) {
                String column = rs.getString("name");
                if (!column.equalsIgnoreCase("id") &&
                        !column.equalsIgnoreCase(String.format("%s", VARIANT_ID))) {
                    columns.add(String.format("%s", column));
                }
            }
        }
        return columns;
    }

    private List<String> getDatabaseFormatColumns() throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(format)");
            while (rs.next()) {
                String column = rs.getString("name");
                if (!column.equalsIgnoreCase("id") &&
                        !column.equalsIgnoreCase("sample_id") &&
                        !column.equalsIgnoreCase(VARIANT_ID)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }
}
