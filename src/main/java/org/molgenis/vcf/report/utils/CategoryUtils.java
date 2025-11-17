package org.molgenis.vcf.report.utils;

import static org.molgenis.vcf.report.utils.JsonUtils.toJson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.repository.DatabaseException;
import org.molgenis.vcf.report.repository.FieldValueKey;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;

public class CategoryUtils {

  private CategoryUtils() {}

  public static Map<FieldValueKey, Integer> loadCategoriesMap(Connection conn) {
    Map<FieldValueKey, Integer> idLookupMap = new HashMap<>();
    String sql = "SELECT id, field, value FROM categories";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        String field = rs.getString("field");
        String value = rs.getString("value");
        int id = rs.getInt("id");
        FieldValueKey key = new FieldValueKey(field, value);
        idLookupMap.put(key, id);
      }
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "load categories");
    }
    return idLookupMap;
  }

  public static void addCategorical(
      FieldType type,
      FieldMetadata meta,
      Map<FieldValueKey, Integer> categoryLookup,
      String field,
      Object val,
      PreparedStatement insertNestedStmt,
      int index)
      throws SQLException {
    addCategorical(type, meta, categoryLookup, field, null, val, insertNestedStmt, index);
  }

  public static void addCategorical(
      FieldType type,
      FieldMetadata meta,
      Map<FieldValueKey, Integer> categoryLookup,
      String field,
      String parent,
      Object val,
      PreparedStatement insertNestedStmt,
      int index)
      throws SQLException {
    String key = getKey(type, field, parent);
    if (val == null || val.equals("")) {
      insertNestedStmt.setString(index, null);
    } else {
      String stringValue = val.toString();
      if (meta.getNumberCount() != null && meta.getNumberCount() == 1) {
        Integer category = categoryLookup.get(new FieldValueKey(key, stringValue));
        if (category != null) {
          insertNestedStmt.setInt(index, category);
        } else {
          throw new UnexpectedCategoryException(key, stringValue);
        }
      } else {
        List<Integer> categories = new ArrayList<>();
        String separator = meta.getSeparator() != null ? meta.getSeparator().toString() : ",";
        for (String singleValue : stringValue.split(separator)) {
          categories.add(categoryLookup.get(new FieldValueKey(key, singleValue)));
        }
        stringValue = toJson(categories);
        insertNestedStmt.setString(index, stringValue);
      }
    }
  }

  public static String getKey(FieldType type, String field, String parent) {
    String key;
    if (parent != null) {
      key = String.format("%s/%s/%s", type.name(), parent, field);
    } else {
      key = String.format("%s/%s", type.name(), field);
    }
    return key;
  }
}
