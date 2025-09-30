package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


@Component
public class ReportMetadataRepository {

    public void insertReportMetadata(Connection conn, ReportMetadata reportMetadata) {
        String sql = "INSERT INTO appMetadata (id, value) VALUES (?, ?)";
        ObjectMapper objectMapper = new ObjectMapper();
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
            insertStmt.setString(1, "htsFile");
            insertStmt.setString(2, objectMapper.writeValueAsString(reportMetadata.getHtsFile()));
            insertStmt.addBatch();


            insertStmt.executeBatch();

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }
}