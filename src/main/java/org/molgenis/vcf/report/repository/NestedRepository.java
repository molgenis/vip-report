package org.molgenis.vcf.report.repository;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.report.utils.CategoryUtils.addCategorical;
import static org.molgenis.vcf.report.utils.CategoryUtils.loadCategoriesMap;
import static org.molgenis.vcf.report.utils.JsonUtils.writeJsonListValue;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;

import htsjdk.variant.variantcontext.VariantContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.molgenis.vcf.utils.metadata.UnknownFieldException;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.springframework.stereotype.Component;

@Component
public class NestedRepository {

  public static final String CSQ_INDEX = "CsqIndex";

  public void insertNested(Connection conn, String fieldName, VariantContext vc,
      List<String> matchingNestedFields,
      FieldMetadatas fieldMetadatas, int variantId, boolean hasDecisionTree) {
    try {
      Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);
      if (fieldName.equals("CSQ")) {
        matchingNestedFields.add(CSQ_INDEX);
      }
      if (vc.hasAttribute(fieldName)) {
        try (PreparedStatement insertNestedStmt = prepareInsertSQL(conn,
            String.format("variant_%s", fieldName), matchingNestedFields)) {
          List<String> nestedEntries = vc.getAttributeAsStringList(fieldName, "");
          insertNestedStmt.setInt(1, variantId);
          int index = 0;
          for (String nestedField : nestedEntries) {
            insertNestedValue(index, matchingNestedFields, nestedField, fieldName,
                fieldMetadatas.getInfo().get(fieldName), insertNestedStmt, categoryLookup,
                hasDecisionTree);
            index++;
          }
          insertNestedStmt.executeBatch();
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert nested values");
    }
  }

  private static void insertNestedValue(int index, List<String> matchingNestedFields,
      String nestedStringValue, String parent, FieldMetadata parentMeta,
      PreparedStatement insertNestedStmt, Map<FieldValueKey, Integer> categoryLookup,
      boolean hasDecisionTree) throws SQLException {
    String separator =
        (parentMeta.getNestedAttributes() != null) ? parentMeta.getNestedAttributes().getSeparator()
            : "|";
    String[] nestedValues = nestedStringValue.split(Pattern.quote(separator), -1);
    int i = 0;
    for (String nestedField : matchingNestedFields) {
      int stmtIdx = i + 2;
      if (nestedField.equals(CSQ_INDEX)) {
        insertNestedStmt.setInt(stmtIdx, index);
      } else {
        NestedFieldMetadata meta = parentMeta.getNestedFields().get(nestedField);
        if (meta == null) {
          throw new UnknownFieldException(nestedField);
        }
        int nestedIndex = meta.getIndex();
        String val =
            (nestedIndex >= 0 && nestedIndex < nestedValues.length) ? nestedValues[nestedIndex]
                : null;

        if (val == null || val.isEmpty()) {
          insertNestedStmt.setString(stmtIdx, null);
        } else if (meta.getType() == CATEGORICAL || nestedField.equals("HPO") || (
            nestedField.equals("VIPC") && hasDecisionTree)) {
          addCategorical(INFO, meta, categoryLookup, nestedField, parent, val, insertNestedStmt,
              stmtIdx);
        } else if (meta.getSeparator() != null) {
          String jsonVal = writeJsonListValue(val, meta.getSeparator().toString());
          insertNestedStmt.setString(stmtIdx, jsonVal);
        } else {
          insertNestedStmt.setString(stmtIdx, val);
        }
      }
      i++;
    }
    insertNestedStmt.addBatch();
  }

  private PreparedStatement prepareInsertSQL(Connection conn, String table, List<String> columns)
      throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (")
        .append(VARIANT_ID);
    for (String col : columns) {
      sql.append(", ").append(col);
    }
    sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
    return conn.prepareStatement(sql.toString());
  }
}
