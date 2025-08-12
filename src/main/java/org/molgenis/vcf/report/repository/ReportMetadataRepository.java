package org.molgenis.vcf.report.repository;

import org.molgenis.vcf.report.model.metadata.ReportMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReportMetadataRepository {

    private final Connection conn;

    public ReportMetadataRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertReportMetadata(ReportMetadata reportMetadata) {
        String sql = "INSERT INTO reportMetadata (id, value) VALUES (?, ?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {


            insertStmt.setString(1, "appArguments");
            insertStmt.setString(2, reportMetadata.getAppMetadata().getAppArguments());
            insertStmt.addBatch();
            insertStmt.setString(1, "name");
            insertStmt.setString(2, reportMetadata.getAppMetadata().getAppName());
            insertStmt.addBatch();
            insertStmt.setString(1, "version");
            insertStmt.setString(2, reportMetadata.getAppMetadata().getAppVersion());
            insertStmt.addBatch();


            insertStmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting config data", e); // FIXME
        }
    }
}