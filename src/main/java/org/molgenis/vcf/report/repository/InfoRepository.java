package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;
import static org.molgenis.vcf.utils.metadata.ValueType.FLAG;

@Component
public class InfoRepository {

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

    public void insertInfoData(
            Connection conn, VariantContext vc,
            List<String> infoColumns,
            FieldMetadatas fieldMetadatas,
            int variantId
    ) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);

        try (PreparedStatement insertInfo = prepareInsertInfo(conn, infoColumns)) {
            insertInfo.setInt(1, variantId);
            for (int i = 0; i < infoColumns.size(); i++) {
                insertInfoDataColumn(vc, infoColumns, fieldMetadatas, i, insertInfo, categoryLookup);
            }
            insertInfo.executeUpdate();
        }
    }

    private static void insertInfoDataColumn(VariantContext vc, List<String> infoColumns, FieldMetadatas fieldMetadatas, int i, PreparedStatement insertInfo, Map<FieldValueKey, Integer> categoryLookup) throws SQLException {
        final String key = infoColumns.get(i);
        final FieldMetadata meta = fieldMetadatas.getInfo().get(key);
        Object value = vc.getAttribute(key, null);

        if(meta.getType() == FLAG) {
            String flagVal = (value == null) ? "0" : "1";
            insertInfo.setString(i + 2, flagVal);
        } else if(meta.getType() == CATEGORICAL) {
            addCategorical(meta, categoryLookup, key, value, insertInfo, i + 2);
        } else if((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null) {
            String separator = (meta.getSeparator() != null) ? meta.getSeparator().toString() : ",";
            Object[] arr = (value instanceof ArrayList)
                    ? ((ArrayList<?>) value).toArray()
                    : value.toString().split(separator);
            String jsonValue = toJson(arr);
            insertInfo.setString(i + 2, jsonValue);
        } else {
            insertInfo.setString(i + 2, value != null ? value.toString() : null);
        }
    }

    private static String toJson(Object arr) {
        try {
            return new ObjectMapper().writeValueAsString(arr);
        } catch (JsonProcessingException e) {
            throw new JsonException(arr.toString());
        }
    }

    private PreparedStatement prepareInsertInfo(Connection conn, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO info (variant_id");
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }
}
