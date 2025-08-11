package org.molgenis.vcf.report.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DecisionTreeRepository {

    private final Connection conn;

    public DecisionTreeRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertDecisionTreeData(Path decisionTreePath, Path sampleTreePath) {
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

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting decision tree data", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading decision tree files", e);
        }
    }
}
