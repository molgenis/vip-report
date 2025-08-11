package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class MetadataRepository {

    private final Connection conn;

    public MetadataRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertMetadata(FieldMetadatas fieldMetadatas) throws SQLException {
        String sql = """
                INSERT INTO metadata (
                    name,
                    fieldType,
                    valueType,
                    numberType,
                    numberCount,
                    required,
                    separator,
                    categories,
                    label,
                    description,
                    parent,
                    nested,
                    nullValue
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<String, FieldMetadata> entry : fieldMetadatas.getFormat().entrySet()) {
                addMetadata(entry, ps, FieldType.FORMAT, null);
            }
            for (Map.Entry<String, FieldMetadata> entry : fieldMetadatas.getInfo().entrySet()) {
                addMetadata(entry, ps, FieldType.INFO, null);
            }

            ps.executeBatch();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMetadata(Map.Entry<String, ? extends FieldMetadata> entry,
                             PreparedStatement ps,
                             FieldType type,
                             String parent) throws SQLException, JsonProcessingException {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        FieldMetadata meta = entry.getValue();
        String fieldName = entry.getKey();

        ps.setString(1, fieldName);
        ps.setString(2, type.name());
        ps.setString(3, meta.getType().name());
        ps.setString(4, meta.getNumberType().name());
        ps.setObject(5, meta.getNumberCount());
        ps.setInt(6, meta.isRequired() ? 1 : 0);
        ps.setString(7, meta.getSeparator() != null ? meta.getSeparator().toString() : null);
        ps.setString(8, meta.getCategories() != null ? ow.writeValueAsString(meta.getCategories()) : null);
        ps.setString(9, meta.getLabel());
        ps.setString(10, meta.getDescription());
        ps.setString(11, parent);
        boolean nestedFlag = meta.getNestedFields() != null && !meta.getNestedFields().isEmpty();
        ps.setInt(12, nestedFlag ? 1 : 0);
        ps.setString(13, meta.getNullValue() != null ? ow.writeValueAsString(meta.getNullValue()) : null);
        ps.addBatch();

        if (nestedFlag) {
            for (Map.Entry<String, NestedFieldMetadata> nestedEntry : meta.getNestedFields().entrySet()) {
                addMetadata(nestedEntry, ps, type, fieldName);
            }
        }
    }
}
