package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.sql.*;
import java.util.*;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;
import static org.molgenis.vcf.utils.metadata.ValueType.FLAG;

public class VcfRepository {

    public static final String MISSING = ".";
    private final Connection conn;

    public VcfRepository(Connection conn) {
        this.conn = conn;
    }

    public Map<FieldValueKey, Integer> loadCategoriesMap() throws SQLException {
        Map<FieldValueKey, Integer> idLookupMap = new HashMap<>();
        String sql = "SELECT id, field, value FROM categories"; // replace "your_table" with actual table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String field = rs.getString("field");
                String value = rs.getString("value");
                int id = rs.getInt("id");
                FieldValueKey key = new FieldValueKey(field, value);
                idLookupMap.put(key, id);
            }
        }
        return idLookupMap;
    }

    public int insertVariant(VariantContext vc) throws SQLException {
        try (PreparedStatement insertVCF = conn.prepareStatement(
                "INSERT INTO vcf (chrom, pos, id_vcf, ref, alt, qual, filter) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ObjectMapper objectMapper = new ObjectMapper();
            insertVCF.setString(1, vc.getContig());
            insertVCF.setInt(2, vc.getStart());
            insertVCF.setString(3, writeJsonListValue(vc.getID(), ","));
            insertVCF.setString(4, vc.getReference().getDisplayString());
            insertVCF.setString(5, objectMapper.writeValueAsString(vc.getAlternateAlleles().stream().map(Allele::getDisplayString).toList()));
            insertVCF.setDouble(6, vc.hasLog10PError() ? vc.getPhredScaledQual() : 0.0);
            if (!vc.filtersWereApplied()) {
                insertVCF.setString(7, null);
            }
            else {
                insertVCF.setString(7, vc.isNotFiltered() ? "PASS" : String.join(",", vc.getFilters()));
            }

            insertVCF.executeUpdate();

            try (ResultSet rs = insertVCF.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve variant_id from vcf insert.");
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); //FIXME
        }
    }

    private static String writeJsonListValue(String value, String separator) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return !value.equals(MISSING) ? objectMapper.writeValueAsString(value.split(separator)) : "[]";
    }

    public void insertNested(String fieldName, VariantContext vc, List<String> matchingNestedFields,
                             FieldMetadatas fieldMetadatas, int variantId) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap();
        if (vc.hasAttribute(fieldName)) {
            try (PreparedStatement insertNestedStmt = prepareInsertSQL(String.format("variant_%s", fieldName), matchingNestedFields)) {
                List<String> nestedEntries = vc.getAttributeAsStringList(fieldName, "");
                insertNestedStmt.setInt(1, variantId);
                for (String nested : nestedEntries) {
                    insertNestedValue(matchingNestedFields, nested, fieldMetadatas.getInfo().get(fieldName), insertNestedStmt, categoryLookup);
                }
                insertNestedStmt.executeBatch();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);//FIXME
            }
        }
    }

    private static void insertNestedValue(List<String> matchingCsqFields, String nestedStringValue, FieldMetadata parent, PreparedStatement insertNestedStmt, Map<FieldValueKey, Integer> categoryLookup) throws SQLException, JsonProcessingException {
        String[] nestedValues = nestedStringValue.split(parent.getSeparator() != null ? parent.getSeparator().toString() : "\\|", -1);
        int i = 0;
        for (String nestedField : matchingCsqFields) {
            NestedFieldMetadata meta = parent.getNestedFields().get(nestedField);
            int nestedIndex = meta.getIndex();
            String val = (nestedIndex >= 0 && nestedIndex < nestedValues.length) ? nestedValues[nestedIndex] : null;
            int stmtIdx = i + 2;

            if (val == null || val.isEmpty()) {
                insertNestedStmt.setString(stmtIdx, null);
            } else if (meta.getType() == CATEGORICAL) {
                addCategorical(meta, categoryLookup, nestedField, val, insertNestedStmt, stmtIdx);
            } else if (meta.getSeparator() != null) {
                String jsonVal = writeJsonListValue(val, meta.getSeparator().toString());
                insertNestedStmt.setString(stmtIdx, jsonVal);
            } else {
                insertNestedStmt.setString(stmtIdx, val);
            }
            i++;
        }
        insertNestedStmt.addBatch();
    }

    private static void addCategorical(FieldMetadata meta, Map<FieldValueKey, Integer> categoryLookup, String field, Object val, PreparedStatement insertNestedStmt, int index) throws SQLException, JsonProcessingException {
        if (val == null) {
            insertNestedStmt.setString(index, null);
        } else {
            String stringValue = val.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            if (meta.getNumberCount() != null && meta.getNumberCount() == 1) {
                Integer category = categoryLookup.get(new FieldValueKey(field, stringValue));
                insertNestedStmt.setInt(index, category);
            } else {
                List<Integer> categories = new ArrayList<>();
                for (String singleValue : stringValue.split(meta.getSeparator().toString())) {
                    categories.add(categoryLookup.get(new FieldValueKey(field, singleValue)));
                }
                stringValue = objectMapper.writeValueAsString(categories);
                insertNestedStmt.setString(index, stringValue);
            }
        }
    }

    public static String getOriginalGTString(Genotype genotype, VariantContext variantContext) {
        StringBuilder gtString = new StringBuilder();
        String sep = genotype.isPhased() ? "|" : "/";

        for (int i = 0; i < genotype.getPloidy(); i++) {
            if (i > 0) gtString.append(sep);
            Allele allele = genotype.getAllele(i);
            int alleleIndex = variantContext.getAlleleIndex(allele);
            gtString.append(alleleIndex);
        }
        return gtString.toString();
    }

    public void insertFormatData(VariantContext vc, List<String> formatColumns, int variantId, FieldMetadatas fieldMetadatas, List<Sample> samples) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap();
        try (PreparedStatement insertFormat = prepareInsertFormat(formatColumns)) {
            insertFormat.setInt(1, variantId);
            for (Genotype genotype : vc.getGenotypes()) {
                Sample sample = samples.stream().filter(s -> s.getPerson().getIndividualId()
                        .equals(genotype.getSampleName())).toList().getFirst();
                int sampleId = sample.getIndex();
                insertFormat.setInt(2, sampleId);
                for (int i = 0; i < formatColumns.size(); i++) {
                    insertFormatDataColumn(vc, formatColumns, fieldMetadatas, genotype, i, categoryLookup, insertFormat);
                }
                insertFormat.addBatch();
            }
            insertFormat.executeBatch();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertFormatDataColumn(VariantContext vc, List<String> formatColumns, FieldMetadatas fieldMetadatas,
                                               Genotype genotype, int i, Map<FieldValueKey, Integer> categoryLookup, PreparedStatement insertFormat
    ) throws SQLException, JsonProcessingException {

        final String key = formatColumns.get(i);
        final FieldMetadata meta = fieldMetadatas.getFormat().get(key);
        Object value = genotype.hasAnyAttribute(key) ? genotype.getAnyAttribute(key) : null;

        if (meta.getType() == CATEGORICAL) {
            addCategorical(meta, categoryLookup, key, value, insertFormat, i + 3);
        } else {
            value = getFormatValue(vc, genotype, meta, value, key);
            String dbValue;
            if (value instanceof Iterable<?>) {
                dbValue = new ObjectMapper().writeValueAsString(value);
            } else {
                dbValue = value != null ? value.toString() : null;
            }
            insertFormat.setString(i + 3, dbValue);
        }
    }

    private static Object getFormatValue(VariantContext vc, Genotype genotype, FieldMetadata meta, Object value, String key) {
        if ((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null && !(value instanceof Iterable<?>)) {
            String separator = meta.getSeparator() != null ? meta.getSeparator().toString() : ",";
            value = List.of(value.toString().split(separator));
        }
        if (value != null && "GT".equals(key)) {
            value = getOriginalGTString(genotype, vc);
        }
        return value;
    }

    public void insertInfoData(
            VariantContext vc,
            List<String> infoColumns,
            FieldMetadatas fieldMetadatas,
            int variantId
    ) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap();

        try (PreparedStatement insertInfo = prepareInsertInfo(infoColumns)) {
            insertInfo.setInt(1, variantId);
            for (int i = 0; i < infoColumns.size(); i++) {
                insertInfoDataColumn(vc, infoColumns, fieldMetadatas, i, insertInfo, categoryLookup);
            }
            insertInfo.executeUpdate();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // FIXME: improve error handling/logging
        }
    }

    private static void insertInfoDataColumn(VariantContext vc, List<String> infoColumns, FieldMetadatas fieldMetadatas, int i, PreparedStatement insertInfo, Map<FieldValueKey, Integer> categoryLookup) throws SQLException, JsonProcessingException {
        final String key = infoColumns.get(i);
        final FieldMetadata meta = fieldMetadatas.getInfo().get(key);
        Object value = vc.getAttribute(key, null);

        if (meta.getType() == FLAG) {
            String flagVal = (value == null) ? "0" : "1";
            insertInfo.setString(i + 2, flagVal);
        } else if (meta.getType() == CATEGORICAL) {
            addCategorical(meta, categoryLookup, key, value, insertInfo, i + 2);
        } else if ((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null) {
            String separator = (meta.getSeparator() != null) ? meta.getSeparator().toString() : ",";
            Object[] arr = (value instanceof ArrayList)
                    ? ((ArrayList<?>) value).toArray()
                    : value.toString().split(separator);
            String jsonValue = new ObjectMapper().writeValueAsString(arr);
            insertInfo.setString(i + 2, jsonValue);
        } else {
            insertInfo.setString(i + 2, value != null ? value.toString() : null);
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
}
