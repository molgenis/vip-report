package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecisionTreeRepositoryTest {

  private Connection conn;
  private PreparedStatement stmt;
  private DecisionTreeRepository repo;

  @BeforeEach
  void setUp() throws Exception {
    conn = mock(Connection.class);
    stmt = mock(PreparedStatement.class);
    when(conn.prepareStatement(anyString())).thenReturn(stmt);
    repo = new DecisionTreeRepository();
  }

  @Test
  void testInsertDecisionTreeDataWithBothPaths() throws Exception {
    Path treePath = mock(Path.class);
    Path samplePath = mock(Path.class);

    try (MockedStatic<java.nio.file.Files> files = mockStatic(java.nio.file.Files.class)) {
      files.when(() -> java.nio.file.Files.readString(treePath)).thenReturn("{tree:1}");
      files.when(() -> java.nio.file.Files.readString(samplePath)).thenReturn("{tree:2}");

      doNothing().when(stmt).addBatch();
      doReturn(new int[]{1}).when(stmt).executeBatch();

      repo.insertDecisionTreeData(conn, treePath, samplePath);

      verify(stmt).setString(1, "decisionTree");
      verify(stmt).setString(2, "{tree:1}");
      verify(stmt).setString(1, "sampleDecisionTree");
      verify(stmt).setString(2, "{tree:2}");
      verify(stmt, times(2)).addBatch();

      verify(stmt, times(1)).executeBatch();
    }
  }

  @Test
  void testInsertDecisionTreeDataWithNulls() throws Exception {
    Path treePath = mock(Path.class);

    try (MockedStatic<java.nio.file.Files> files = mockStatic(java.nio.file.Files.class)) {
      files.when(() -> java.nio.file.Files.readString(treePath)).thenReturn("{tree:1}");

      doNothing().when(stmt).addBatch();
      doReturn(new int[]{1}).when(stmt).executeBatch();

      repo.insertDecisionTreeData(conn, treePath, null);

      verify(stmt).setString(1, "decisionTree");
      verify(stmt).setString(2, "{tree:1}");
      verify(stmt, times(1)).addBatch();

      verify(stmt, never()).setString(1, "sampleDecisionTree");
      verify(stmt, times(1)).executeBatch();
    }
  }
}
