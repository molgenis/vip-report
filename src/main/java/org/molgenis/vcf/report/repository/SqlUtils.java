package org.molgenis.vcf.report.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class SqlUtils {

  private SqlUtils() {}

  public static Map<Object, Integer> insertLookupValues(
      Connection conn, String tableName, Iterable<?> values) {
    Map<Object, Integer> idMap = new HashMap<>();
    String sql = String.format("INSERT INTO \"%s\" (\"id\", \"value\") VALUES (?, ?)", tableName);
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      int i = 0;
      for (Object value : values) {
        ps.setInt(1, i);
        ps.setString(2, value.toString());
        ps.addBatch();
        idMap.put(value, i);
        i++;
      }
      ps.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(
          e.getMessage(), String.format("insertLookupValues for: \"%s\"", tableName));
    }
    return idMap;
  }

  public static String quote(String input) {
    return String.format("\"%s\"", input);
  }

  public static List<?> replaceMissingValueWithNull(Iterable<?> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false)
        .map(value -> ".".equals(value) ? null : value)
        .toList();
  }

  public static List<Object> replaceMissingValueWithNull(Object[] valueArray) {
    return Arrays.stream(valueArray).map(value -> ".".equals(value) ? null : value).toList();
  }
}
