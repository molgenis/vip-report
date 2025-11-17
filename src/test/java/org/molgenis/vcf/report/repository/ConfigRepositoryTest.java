package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfigRepositoryTest {

  private Connection conn;
  private PreparedStatement insertStmt;
  private ConfigRepository repo;

  @BeforeEach
  void setUp() throws SQLException {
    conn = mock(Connection.class);
    insertStmt = mock(PreparedStatement.class);
    when(conn.prepareStatement(anyString())).thenReturn(insertStmt);
    repo = new ConfigRepository();
  }

  @Test
  void testInsertConfigDataWithPrimitives() throws Exception {
    Map<String, Object> config = Map.of(
        "id1", "string",
        "id2", 12345
    );
    doNothing().when(insertStmt).addBatch();
    doReturn(new int[]{1}).when(insertStmt).executeBatch();

    repo.insertConfigData(conn, config);

    verify(conn, times(1)).prepareStatement(anyString());
    verify(insertStmt).setString(1, "id2");
    verify(insertStmt).setString(2, "12345");
    verify(insertStmt).setString(1, "id1");
    verify(insertStmt).setString(2, "string");
    verify(insertStmt, times(config.size())).addBatch();
    verify(insertStmt, times(1)).executeBatch();
  }

  @Test
  void testInsertConfigDataWithMapAndIterable() throws Exception {
    Map<String, Object> config = new HashMap<>();
    config.put("id1", Map.of("key", "value"));
    config.put("id2", List.of(1, 2, 3));

    doNothing().when(insertStmt).addBatch();
    doReturn(new int[]{1}).when(insertStmt).executeBatch();

    repo.insertConfigData(conn, config);

    verify(conn, times(1)).prepareStatement(anyString());
    verify(insertStmt).setString(1, "id1");
    verify(insertStmt).setString(2, "{\"key\":\"value\"}");
    verify(insertStmt).setString(1, "id2");
    verify(insertStmt).setString(2, "[1,2,3]");
    verify(insertStmt, times(config.size())).addBatch();
    verify(insertStmt, times(1)).executeBatch();
  }
}
