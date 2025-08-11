package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;

public class VcfRepository {

    private final Connection conn;

    public VcfRepository(Connection conn) {
        this.conn = conn;
    }

    public int insertVariant(VariantContext vc) throws SQLException {
        try (PreparedStatement insertVCF = conn.prepareStatement(
                "INSERT INTO vcf (chrom, pos, id_vcf, ref, alt, qual, filter) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            insertVCF.setString(1, vc.getContig());
            insertVCF.setInt(2, vc.getStart());
            insertVCF.setString(3, vc.getID());
            insertVCF.setString(4, vc.getReference().getDisplayString());
            insertVCF.setString(5, vc.getAlternateAlleles().toString());
            insertVCF.setDouble(6, vc.hasLog10PError() ? vc.getPhredScaledQual() : 0.0);
            insertVCF.setString(7, vc.isFiltered() ? String.join(";", vc.getFilters()) : "PASS");

            insertVCF.executeUpdate();

            // Retrieve generated variant_id
            try (ResultSet rs = insertVCF.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve variant_id from vcf insert.");
                }
            }
        }
    }

    public void insertCsqData(VariantContext vc, List<String> matchingCsqFields,
                              List<String> csqFields, int variantId) throws SQLException {
        if (vc.hasAttribute("CSQ")) {
            try (PreparedStatement insertCSQ = prepareInsertSQL("variant_CSQ", matchingCsqFields)) {
                String[] csqEntries = vc.getAttributeAsString("CSQ", "").split(",");
                for (String csq : csqEntries) {
                    String[] values = csq.split("\\|", -1);
                    insertCSQ.setInt(1, variantId);
                    for (int i = 0; i < matchingCsqFields.size(); i++) {
                        int csqIndex = csqFields.indexOf(matchingCsqFields.get(i));
                        String val = (csqIndex >= 0 && csqIndex < values.length) ? values[csqIndex] : null;
                        insertCSQ.setString(i + 2, val);
                    }
                    insertCSQ.addBatch();
                }
                insertCSQ.executeBatch();
            }
        }
    }

    public void insertFormatData(VariantContext vc, List<String> formatColumns, int variantId) throws SQLException {
        try (PreparedStatement insertFormat = prepareInsertFormat(formatColumns)) {
            for (Genotype genotype : vc.getGenotypes()) {
                insertFormat.setInt(1, variantId);
                insertFormat.setString(2, genotype.getSampleName());
                for (int i = 0; i < formatColumns.size(); i++) {
                    String key = formatColumns.get(i);
                    Object value = genotype.hasAnyAttribute(key) ? genotype.getAnyAttribute(key) : null;
                    if (value == null && "GT".equals(key)) {
                        value = genotype.getGenotypeString();
                    }
                    insertFormat.setString(i + 3, value != null ? value.toString() : null);
                }
                insertFormat.addBatch();
            }
            insertFormat.executeBatch();
        }
    }

    public void insertInfoData(VariantContext vc, List<String> infoColumns, int variantId) throws SQLException {
        try (PreparedStatement insertInfo = prepareInsertInfo(infoColumns)) {
            insertInfo.setInt(1, variantId);
            for (int i = 0; i < infoColumns.size(); i++) {
                String key = infoColumns.get(i);
                Object value = vc.hasAttribute(key) ? vc.getAttribute(key) : null;
                insertInfo.setString(i + 2, value != null ? value.toString() : null);
            }
            insertInfo.executeUpdate();
        }
    }

    private PreparedStatement prepareInsertSQL(String table, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (").append(VARIANT_ID);
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }

    private PreparedStatement prepareInsertFormat(List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO format (variant_id, sample_id");
        for (String column : columns) {
            sql.append(", ").append(column);
        }
        sql.append(") VALUES (?, ?");
        sql.append(", ?".repeat(columns.size()));
        sql.append(")");
        return conn.prepareStatement(sql.toString());
    }

    private PreparedStatement prepareInsertInfo(List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO info (variant_id");
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }

    public List<String> getDatabaseCSQColumns() throws SQLException {
        return getTableColumnsExcluding("variant_CSQ", "id", VARIANT_ID);
    }

    public List<String> getDatabaseFormatColumns() throws SQLException {
        return getTableColumnsExcluding("format", "id", "sample_id", VARIANT_ID);
    }

    public List<String> getDatabaseInfoColumns() throws SQLException {
        return getTableColumnsExcluding("info", "id", VARIANT_ID);
    }

    private List<String> getTableColumnsExcluding(String table, String... excludeColumns) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String col = rs.getString("name");
                boolean excluded = false;
                for (String exc : excludeColumns) {
                    if (col.equalsIgnoreCase(exc)) {
                        excluded = true;
                        break;
                    }
                }
                if (!excluded) {
                    columns.add(col);
                }
            }
        }
        return columns;
    }
}
