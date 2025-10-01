package org.molgenis.vcf.report.repository;

import lombok.NonNull;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.molgenis.vcf.utils.sample.model.OntologyClass;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.molgenis.vcf.report.utils.JsonUtils.collectNodes;
import static org.molgenis.vcf.report.utils.JsonUtils.toJson;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;


@Component
 class MetadataRepository {

    public void insertMetadata(
            Connection conn, FieldMetadatas fieldMetadatas,
            Path decisionTreePath,
            Path sampleTreePath,
            @NonNull List<Phenopacket> phenopackets
    ) throws SQLException {
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
            Map<String, Map<String, ValueDescription>> customCategories = getCustomCategories(decisionTreePath, sampleTreePath, phenopackets);
            insertForFields(conn, fieldMetadatas.getFormat().entrySet(), ps, FieldType.FORMAT, customCategories);
            insertForFields(conn, fieldMetadatas.getInfo().entrySet(), ps, FieldType.INFO, customCategories);
            ps.executeBatch();
        }
    }

    private Map<String, Map<String, ValueDescription>> getCustomCategories(Path decisionTreePath, Path sampleTreePath, List<Phenopacket> phenopackets) {
        Map<String, Map<String, ValueDescription>> customCategories = new HashMap<>();
        if(sampleTreePath != null){
            customCategories.put("VIPC_S", collectNodes(sampleTreePath));
        }
        if(sampleTreePath != null)
            {customCategories.put("VIPC", collectNodes(decisionTreePath));
        }
        customCategories.put("HPO", collectHpos(phenopackets));
        return customCategories;
    }

    private void insertForFields(
            Connection conn,
            Set<? extends Map.Entry<String, FieldMetadata>> entries,
            PreparedStatement ps,
            FieldType fieldType,
            Map<String, Map<String, ValueDescription>> customCategories
    ) throws SQLException {
        for (Map.Entry<String, ? extends FieldMetadata> entry : entries) {
            addMetadata(conn, entry, ps, fieldType, null, customCategories);
        }
    }

    private Map<String, ValueDescription> collectHpos(@NonNull List<Phenopacket> phenopackets) {
        Map<String, ValueDescription> hpos = new HashMap<>();
        for (Phenopacket phenopacket : phenopackets) {
            for (PhenotypicFeature feature : phenopacket.getPhenotypicFeaturesList()) {
                OntologyClass ontologyClass = feature.getOntologyClass();
                if (!hpos.containsKey(ontologyClass.getId())) {
                    hpos.put(ontologyClass.getId(), new ValueDescription(ontologyClass.getId(), ontologyClass.getLabel()));
                }
            }
        }
        return hpos;
    }

    private void addMetadata(
            Connection conn, Map.Entry<String, ? extends FieldMetadata> metadataEntry,
            PreparedStatement ps,
            FieldType type,
            String parent,
            Map<String, Map<String, ValueDescription>> customCategories
    ) throws SQLException {
        FieldMetadata meta = metadataEntry.getValue();
        String fieldName = metadataEntry.getKey();
        Map<String, ValueDescription> categories = getCategories(fieldName, meta, customCategories);

        insertCategoriesBatch(conn, fieldName, categories);

        ps.setString(1, fieldName);
        ps.setString(2, type.name());
        ps.setString(3, customCategories.containsKey(fieldName) ? CATEGORICAL.name() : meta.getType().name());
        ps.setString(4, meta.getNumberType().name());
        ps.setObject(5, meta.getNumberCount());
        ps.setInt(6, meta.isRequired() ? 1 : 0);
        ps.setString(7, meta.getSeparator() != null ? meta.getSeparator().toString() : null);
        ps.setString(8, categories != null ? toJson(categories) : null);
        ps.setString(9, meta.getLabel());
        ps.setString(10, meta.getDescription());
        ps.setString(11, parent);
        boolean nestedFlag = meta.getNestedFields() != null && !meta.getNestedFields().isEmpty();
        ps.setInt(12, nestedFlag ? 1 : 0);
        ps.setString(13, meta.getNullValue() != null ? toJson(meta.getNullValue()) : null);
        ps.addBatch();

        if (nestedFlag) {
            for (Map.Entry<String, NestedFieldMetadata> nestedEntry : meta.getNestedFields().entrySet()) {
                addMetadata(conn, nestedEntry, ps, type, fieldName, customCategories);
            }
        }
    }

    private void insertCategoriesBatch(Connection conn, String fieldName, Map<String, ValueDescription> categories) throws SQLException {
        if (categories != null && !categories.isEmpty()) {
            String sql = """
                    INSERT INTO categories (
                        field,
                        value,
                        label,
                        description
                    ) VALUES (?, ?, ?, ?)
                    """;
            try (PreparedStatement categoryPs = conn.prepareStatement(sql)) {
                categoryPs.setString(1, fieldName);
                for (Map.Entry<String, ValueDescription> entry : categories.entrySet()) {
                    categoryPs.setString(2, entry.getKey());
                    categoryPs.setString(3, entry.getValue().getLabel());
                    categoryPs.setString(4, entry.getValue().getDescription());
                    categoryPs.addBatch();
                }
                categoryPs.executeBatch();
            }
        }
    }

    private Map<String, ValueDescription> getCategories(
            String fieldName,
            FieldMetadata meta,
            Map<String, Map<String, ValueDescription>> customCategories
    ) {
        if (customCategories.containsKey(fieldName)) {
            return customCategories.get(fieldName);
        } else {
            return meta.getCategories() == null ? null : meta.getCategories();
        }
    }

    public void insertHeaderLine(Connection conn, List<String> lines, String headerline) {
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
            throw new DatabaseException(e.getMessage());
        }
    }
}
