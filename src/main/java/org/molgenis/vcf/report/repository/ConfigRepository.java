package org.molgenis.vcf.report.repository;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static org.molgenis.vcf.report.utils.JsonUtils.toJson;

@Component
public class ConfigRepository {

    public void insertConfigData(Connection conn, Map<?, ?> templateConfig) {
        String sql = "INSERT INTO config (id, value) VALUES (?, ?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {

             for (Map.Entry<?, ?> entry : templateConfig.entrySet()) {
                insertStmt.setString(1, entry.getKey().toString());
                Object value = entry.getValue();

                String stringValue;
                if (value instanceof Map || value instanceof Iterable) {
                    stringValue = toJson(value);
                } else {
                    stringValue = value.toString();
                }
                insertStmt.setString(2, stringValue);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), "insert config data");
        }
    }
}
