package org.molgenis.vcf.report.utils;

import org.molgenis.vcf.report.repository.FieldValueKey;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.vcf.report.utils.JsonUtils.toJson;

public class CategoryUtils {

    private CategoryUtils() {}

    public static Map<FieldValueKey, Integer> loadCategoriesMap(Connection conn) throws SQLException {
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

    public static void addCategorical(FieldMetadata meta, Map<FieldValueKey, Integer> categoryLookup, String field, Object val, PreparedStatement insertNestedStmt, int index) throws SQLException {
        if(val == null) {
            insertNestedStmt.setString(index, null);
        } else {
            String stringValue = val.toString();
            if(meta.getNumberCount() != null && meta.getNumberCount() == 1) {
                Integer category = categoryLookup.get(new FieldValueKey(field, stringValue));
                if(category != null) {
                    insertNestedStmt.setInt(index, category);
                }else{
                    insertNestedStmt.setString(index, null);
                }
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
}
