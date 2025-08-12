package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class ConfigRepository {

    private final Connection conn;

    public ConfigRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertConfigData(Map<?, ?> templateConfig) {
        String sql = "INSERT INTO reportdata (id, value) VALUES (?, ?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {

            ObjectMapper mapper = new ObjectMapper();

            for (Map.Entry<?, ?> entry : templateConfig.entrySet()) {
                insertStmt.setString(1, entry.getKey().toString());
                Object value = entry.getValue();

                String stringValue;
                if (value instanceof Map || value instanceof Iterable) {
                    stringValue = mapper.writeValueAsString(value);
                } else {
                    stringValue = value.toString();
                }
                insertStmt.setString(2, stringValue);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error inserting config data", e); // FIXME
        }
    }
}
