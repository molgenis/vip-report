package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;

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
            Map<String, Map<String, ValueDescription>> customCategories = new HashMap<>();
            //FIXME hardcoded
            customCategories.put("VIPC_S", Map.of("U1", new ValueDescription("U1", "U1 desc"),"U2", new ValueDescription("U2", "U2 desc"),"U3", new ValueDescription("U3", "U3 desc")));
            customCategories.put("VIPC", Map.of("LB", new ValueDescription("LB", "LB desc"),"LP", new ValueDescription("LP", "LP desc"),"VUS", new ValueDescription("VUS", "VUS desc"),"P", new ValueDescription("P", "P desc"),"B", new ValueDescription("B", "B desc"),"LQ", new ValueDescription("LQ", "LQ desc")));
            customCategories.put("HPO", Map.of("HP:0001627", new ValueDescription("0001627", "0001627 desc"),"HP:0000951", new ValueDescription("0000951", "0000951 desc")));
            for (Map.Entry<String, FieldMetadata> entry : fieldMetadatas.getFormat().entrySet()) {
                addMetadata(entry, ps, FieldType.FORMAT, null, customCategories);
            }
            for (Map.Entry<String, FieldMetadata> entry : fieldMetadatas.getInfo().entrySet()) {
                addMetadata(entry, ps, FieldType.INFO, null, customCategories);
            }

            ps.executeBatch();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMetadata(Map.Entry<String, ? extends FieldMetadata> metadataEntry,
                             PreparedStatement ps,
                             FieldType type,
                             String parent, Map<String, Map<String, ValueDescription>> customCategories) throws SQLException, JsonProcessingException {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        FieldMetadata meta = metadataEntry.getValue();
        String fieldName = metadataEntry.getKey();
        Map<String, ValueDescription> categories = getCategories(fieldName, meta, customCategories);

        if(categories != null && !categories.isEmpty()){
            String sql = """
                INSERT INTO categories (
                    field,
                    value,
                    label,
                    description
                ) VALUES (?, ?, ?, ?)
                """;

            try (PreparedStatement categoryPs = conn.prepareStatement(sql)) {
                for(Map.Entry<String, ValueDescription> entry : categories.entrySet()){
                    categoryPs.setString(1, fieldName);
                    categoryPs.setString(2, entry.getKey());
                    categoryPs.setString(3, entry.getValue().getLabel());
                    categoryPs.setString(4, entry.getValue().getDescription());
                    categoryPs.addBatch();
                }
                categoryPs.executeBatch();
            }

        }

        ps.setString(1, fieldName);
        ps.setString(2, type.name());
        ps.setString(3, customCategories.containsKey(fieldName) ? CATEGORICAL.name() : meta.getType().name());
        ps.setString(4, meta.getNumberType().name());
        ps.setObject(5, meta.getNumberCount());
        ps.setInt(6, meta.isRequired() ? 1 : 0);
        ps.setString(7, meta.getSeparator() != null ? meta.getSeparator().toString() : null);
        ps.setString(8, categories != null ? ow.writeValueAsString(categories) : null);
        ps.setString(9, meta.getLabel());
        ps.setString(10, meta.getDescription());
        ps.setString(11, parent);
        boolean nestedFlag = meta.getNestedFields() != null && !meta.getNestedFields().isEmpty();
        ps.setInt(12, nestedFlag ? 1 : 0);
        ps.setString(13, meta.getNullValue() != null ? ow.writeValueAsString(meta.getNullValue()) : null);
        ps.addBatch();

        if (nestedFlag) {
            for (Map.Entry<String, NestedFieldMetadata> nestedEntry : meta.getNestedFields().entrySet()) {
                addMetadata(nestedEntry, ps, type, fieldName, customCategories);
            }
        }
    }

    private Map<String, ValueDescription> getCategories(String fieldName, FieldMetadata meta, Map<String, Map<String, ValueDescription>> customCategories) {
        if(customCategories.containsKey(fieldName)){
            return customCategories.get(fieldName);
        }else{
            return meta.getCategories() == null ? null : meta.getCategories();
        }
    }

    public void insertHeaderLine(List<String> lines, String headerline) {
        String sql = "INSERT INTO header (line) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
            for (String line : lines) {
                insertStmt.setString(1, String.format("##%s", line));
                insertStmt.addBatch();
            }
            insertStmt.setString(1, headerline);
            insertStmt.addBatch();

            insertStmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting header data", e); // FIXME
        }
    }
}
