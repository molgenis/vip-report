package org.molgenis.vcf.report.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqlUtils {

  private SqlUtils() {}

  public static Map<Object, Integer> insertLookupValues(
      Connection conn, String tableName, Iterable<?> values) {
    Map<Object, Integer> idMap = new HashMap<>();
    String sql = String.format("INSERT INTO %s (id, value) VALUES (?, ?)", tableName);
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
          e.getMessage(), String.format("insertLookupValues for: '%s'", tableName));
    }
    return idMap;
  }
}
