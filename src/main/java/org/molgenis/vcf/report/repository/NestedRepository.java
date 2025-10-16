package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.metadata.UnknownFieldException;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.report.utils.CategoryUtils.addCategorical;
import static org.molgenis.vcf.report.utils.CategoryUtils.loadCategoriesMap;
import static org.molgenis.vcf.report.utils.JsonUtils.writeJsonListValue;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;

@Component
public class NestedRepository {
    public static final Set<String> CUSTOM_CATEGORICALS = Set.of("VIPC", "HPO");

    public void insertNested(Connection conn, String fieldName, VariantContext vc, List<String> matchingNestedFields,
                             FieldMetadatas fieldMetadatas, int variantId, boolean hasDecisionTree) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);
        if(vc.hasAttribute(fieldName)) {
            try (PreparedStatement insertNestedStmt = prepareInsertSQL(conn, String.format("variant_%s", fieldName), matchingNestedFields)) {
                List<String> nestedEntries = vc.getAttributeAsStringList(fieldName, "");
                insertNestedStmt.setInt(1, variantId);
                for (String nestedField : nestedEntries) {
                    insertNestedValue(matchingNestedFields, nestedField, fieldName, fieldMetadatas.getInfo().get(fieldName), insertNestedStmt, categoryLookup, hasDecisionTree);
                }
                insertNestedStmt.executeBatch();
            }
        }
    }

    private static void insertNestedValue(List<String> matchingCsqFields, String nestedStringValue, String parent, FieldMetadata parentMeta, PreparedStatement insertNestedStmt, Map<FieldValueKey, Integer> categoryLookup, boolean hasDecisionTree) throws SQLException {
        String separator = (parentMeta.getNestedAttributes() != null ) ? parentMeta.getNestedAttributes().getSeparator() : "|";
        String[] nestedValues = nestedStringValue.split(Pattern.quote(separator), -1);
        int i = 0;
        for (String nestedField : matchingCsqFields) {
            NestedFieldMetadata meta = parentMeta.getNestedFields().get(nestedField);
            if(meta == null) {
                throw new UnknownFieldException(nestedField);
            }
            int nestedIndex = meta.getIndex();
            String val = (nestedIndex >= 0 && nestedIndex < nestedValues.length) ? nestedValues[nestedIndex] : null;
            int stmtIdx = i + 2;

            if(val == null || val.isEmpty()) {
                insertNestedStmt.setString(stmtIdx, null);
            } else if(meta.getType() == CATEGORICAL || CUSTOM_CATEGORICALS.contains(nestedField) && hasDecisionTree) {
                addCategorical(INFO, meta, categoryLookup, nestedField, parent, val, insertNestedStmt, stmtIdx);
            } else if(meta.getSeparator() != null) {
                String jsonVal = writeJsonListValue(val, meta.getSeparator().toString());
                insertNestedStmt.setString(stmtIdx, jsonVal);
            } else {
                insertNestedStmt.setString(stmtIdx, val);
            }
            i++;
        }
        insertNestedStmt.addBatch();
    }

    private PreparedStatement prepareInsertSQL(Connection conn, String table, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (").append(VARIANT_ID);
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }
}
