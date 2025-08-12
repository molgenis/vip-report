package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.report.generator.SqlUtils.extractCSQFields;

public class DatabaseManager {
    private Connection conn;
    private static final String FILE = "vip-report.db";
    private static final String DB_URL = String.format("jdbc:sqlite:%s", FILE);
    public static final String VARIANT_ID = "variant_id";

    public DatabaseManager() {
        // Delete the existing file
        try {
            Files.deleteIfExists(Path.of(FILE));
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
        if(conn == null){
            try {
                this.conn = DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {
                throw new RuntimeException(e); //FIXME
            }
        }
    }

    public void populateDb(FieldMetadatas fieldMetadatas, Items<Sample> samples, File vcfFile, Path decisionTreePath, Path sampleTreePath, ReportData reportData, ReportMetadata reportMetadata, Map<?, ?> templateConfig) throws Exception {
        try (
                VCFFileReader reader = new VCFFileReader(vcfFile, false);
        ) {
            conn.setAutoCommit(false);

            VcfRepository vcfRepo = new VcfRepository(conn);
            PhenotypeRepository phenotypeRepo = new PhenotypeRepository(conn);
            MetadataRepository metadataRepo = new MetadataRepository(conn);
            ConfigRepository configRepo = new ConfigRepository(conn);
            DecisionTreeRepository decisionTreeRepo = new DecisionTreeRepository(conn);
            SampleRepository sampleRepo = new SampleRepository(conn);
            ReportMetadataRepository reportMetadataRepo = new ReportMetadataRepository(conn);
            VCFHeader header = reader.getFileHeader();

            // Extract CSQ field names from VCF header
            String csqDesc = header.getInfoHeaderLine("CSQ").getDescription();
            List<String> csqFields = extractCSQFields(csqDesc);

            // Filter CSQ fields that match your database schema
            List<String> dbCsqColumns = getDatabaseCSQColumns();
            List<String> matchingCsqFields = new ArrayList<>();
            for (String field : csqFields) {
                if (dbCsqColumns.contains(field)) {
                    matchingCsqFields.add(field);
                } else {
                    throw new IllegalArgumentException(field);
                }
            }
            List<String> formatColumns = getDatabaseFormatColumns();
            List<String> infoColumns = getDatabaseInfoColumns();

            for (VariantContext vc : reader) {
                int variantId = vcfRepo.insertVariant(vc);
                vcfRepo.insertCsqData(vc, matchingCsqFields, dbCsqColumns, variantId);
                vcfRepo.insertFormatData(vc, formatColumns, variantId);
                vcfRepo.insertInfoData(vc, infoColumns, variantId);
            }
            metadataRepo.insertMetadata(fieldMetadatas);
            sampleRepo.insertSamples(samples);
            phenotypeRepo.insertPhenotypeData(reportData);
            configRepo.insertConfigData(templateConfig);
            decisionTreeRepo.insertDecisionTreeData(decisionTreePath, sampleTreePath);
            reportMetadataRepo.insertReportMetadata(reportMetadata);

            conn.commit();
        }
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

    private List<String> getDatabaseCSQColumns() throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(variant_CSQ)");
            while (rs.next()) {
                String column = rs.getString("name");
                if (!column.equalsIgnoreCase("id") && !column.equalsIgnoreCase(VARIANT_ID)) {
                    columns.add(column);
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
