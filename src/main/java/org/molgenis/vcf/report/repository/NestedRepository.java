package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.metadata.UnknownFieldException;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;

@Component
public class NestedRepository {

    public static final String MISSING = ".";

    private Map<FieldValueKey, Integer> loadCategoriesMap(Connection conn) throws SQLException {
        Map<FieldValueKey, Integer> idLookupMap = new HashMap<>();
        String sql = "SELECT id, field, value FROM categories";
        try (
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
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

    private static String writeJsonListValue(String value, String separator){
        return !value.equals(MISSING) ? toJson(value.split(separator)) : "[]";
    }

    public void insertNested(Connection conn, String fieldName, VariantContext vc, List<String> matchingNestedFields,
                             FieldMetadatas fieldMetadatas, int variantId) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);
        if(vc.hasAttribute(fieldName)) {
            try (PreparedStatement insertNestedStmt = prepareInsertSQL(conn, String.format("variant_%s", fieldName), matchingNestedFields)) {
                List<String> nestedEntries = vc.getAttributeAsStringList(fieldName, "");
                insertNestedStmt.setInt(1, variantId);
                for (String nested : nestedEntries) {
                    insertNestedValue(matchingNestedFields, nested, fieldMetadatas.getInfo().get(fieldName), insertNestedStmt, categoryLookup);
                }
                insertNestedStmt.executeBatch();
            }
        }
    }

    private static void insertNestedValue(List<String> matchingCsqFields, String nestedStringValue, FieldMetadata parent, PreparedStatement insertNestedStmt, Map<FieldValueKey, Integer> categoryLookup) throws SQLException {
        String separator = parent.getSeparator() != null ? parent.getSeparator().toString() : "|";
        String[] nestedValues = nestedStringValue.split(Pattern.quote(separator), -1);
        int i = 0;
        for (String nestedField : matchingCsqFields) {
            NestedFieldMetadata meta = parent.getNestedFields().get(nestedField);
            if(meta == null) {
                throw new UnknownFieldException(nestedField);
            }
            int nestedIndex = meta.getIndex();
            String val = (nestedIndex >= 0 && nestedIndex < nestedValues.length) ? nestedValues[nestedIndex] : null;
            int stmtIdx = i + 2;

            if(val == null || val.isEmpty()) {
                insertNestedStmt.setString(stmtIdx, null);
            } else if(meta.getType() == CATEGORICAL) {
                addCategorical(meta, categoryLookup, nestedField, val, insertNestedStmt, stmtIdx);
            } else if(meta.getSeparator() != null) {
                String jsonVal = writeJsonListValue(val, meta.getSeparator().toString());
                insertNestedStmt.setString(stmtIdx, jsonVal);
            } else {
                insertNestedStmt.setString(stmtIdx, val);
            }
            i++;
        }
        insertNestedStmt.addBatch();
    }

    private static void addCategorical(FieldMetadata meta, Map<FieldValueKey, Integer> categoryLookup, String field, Object val, PreparedStatement insertNestedStmt, int index) throws SQLException {
        if(val == null) {
            insertNestedStmt.setString(index, null);
        } else {
            String stringValue = val.toString();
            if(meta.getNumberCount() != null && meta.getNumberCount() == 1) {
                Integer category = categoryLookup.get(new FieldValueKey(field, stringValue));
                insertNestedStmt.setInt(index, category);
            } else {
                List<Integer> categories = new ArrayList<>();
                for(String singleValue : stringValue.split(meta.getSeparator().toString())) {
                    categories.add(categoryLookup.get(new FieldValueKey(field, singleValue)));
                }
                stringValue = toJson(categories);
                insertNestedStmt.setString(index, stringValue);
            }
        }
    }

    private static String toJson(Object arr) {
        try {
            return new ObjectMapper().writeValueAsString(arr);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }

    private PreparedStatement prepareInsertSQL(Connection conn, String table, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (").append(VARIANT_ID);
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }
}
