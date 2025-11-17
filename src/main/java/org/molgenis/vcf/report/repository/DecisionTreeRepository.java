package org.molgenis.vcf.report.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.stereotype.Component;

@Component
public class DecisionTreeRepository {

  public void insertDecisionTreeData(Connection conn, Path decisionTreePath, Path sampleTreePath) {
    String sql = "INSERT INTO decisiontree (id, tree) VALUES (?, ?)";

    try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
      if (decisionTreePath != null) {
        insertStmt.setString(1, "decisionTree");
        insertStmt.setString(2, Files.readString(decisionTreePath));
        insertStmt.addBatch();
      }
      if (sampleTreePath != null) {
        insertStmt.setString(1, "sampleDecisionTree");
        insertStmt.setString(2, Files.readString(sampleTreePath));
        insertStmt.addBatch();
      }
      insertStmt.executeBatch();

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert decision tree");
    }
  }
}
