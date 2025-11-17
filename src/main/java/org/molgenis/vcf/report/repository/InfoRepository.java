package org.molgenis.vcf.report.repository;

import static java.util.Collections.emptyMap;
import static org.molgenis.vcf.report.repository.FormatRepository.VIPC_S;
import static org.molgenis.vcf.report.utils.CategoryUtils.addCategorical;
import static org.molgenis.vcf.report.utils.CategoryUtils.loadCategoriesMap;
import static org.molgenis.vcf.report.utils.JsonUtils.MISSING;
import static org.molgenis.vcf.report.utils.JsonUtils.toJson;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;
import static org.molgenis.vcf.utils.metadata.ValueType.FLAG;

import htsjdk.variant.variantcontext.VariantContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.springframework.stereotype.Component;

@Component
public class InfoRepository {

  public void insertInfoData(
      Connection conn, VariantContext vc,
      List<String> infoColumns,
      FieldMetadatas fieldMetadatas,
      int variantId, boolean hasSampleTree
  ) {
    Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);

    try (PreparedStatement insertInfo = prepareInsertInfo(conn, infoColumns)) {
      insertInfo.setInt(1, variantId);
      for (int i = 0; i < infoColumns.size(); i++) {
        insertInfoDataColumn(vc, infoColumns, fieldMetadatas, i, insertInfo, categoryLookup,
            hasSampleTree);
      }
      insertInfo.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert info values");
    }
  }

  private static void insertInfoDataColumn(VariantContext vc, List<String> infoColumns,
      FieldMetadatas fieldMetadatas, int i, PreparedStatement insertInfo,
      Map<FieldValueKey, Integer> categoryLookup, boolean hasSampleTree) throws SQLException {
    final String key = infoColumns.get(i);
    final FieldMetadata meta = fieldMetadatas.getInfo().get(key);
    Object value = vc.getAttribute(key, null);

    if (meta.getType() == FLAG) {
      int flagVal = (value == null) ? 0 : 1;
      insertInfo.setInt(i + 2, flagVal);
    } else if (meta.getType() == CATEGORICAL || (VIPC_S.equals(key) && hasSampleTree)) {
      addCategorical(INFO, meta, categoryLookup, key, value, insertInfo, i + 2);
    } else if ((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null) {
      String separator = (meta.getSeparator() != null) ? meta.getSeparator().toString() : ",";
      Object[] arr = (value instanceof ArrayList)
          ? ((ArrayList<?>) value).toArray()
          : value.toString().split(separator);
      String jsonValue = toJson(arr);
      insertInfo.setString(i + 2, jsonValue);
    } else {
      insertInfo.setString(i + 2, value != null ? value.toString() : null);
    }
  }

  private PreparedStatement prepareInsertInfo(Connection conn, List<String> columns)
      throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO info (variantId");
    for (String col : columns) {
      sql.append(", ").append(col);
    }
    sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
    return conn.prepareStatement(sql.toString());
  }

  public void insertInfoFieldOrder(Connection conn,
      Map<FieldType, Map<String, Integer>> metadataKeys, String[] infoItems, int variantId) {
    String INSERT_INFO_ORDER_SQL = "INSERT INTO infoOrder (infoIndex, variantId, metadataId) VALUES (?, ?, ?)";

    try (PreparedStatement pstmt = conn.prepareStatement(INSERT_INFO_ORDER_SQL)) {
      if (infoItems.length == 0 || (infoItems.length == 1 && infoItems[0].equals(MISSING))) {
        return;//No info order if INFO column is a missing value.
      }
      pstmt.setInt(2, variantId);
      for (int i = 0; i < infoItems.length; i++) {
        String item = infoItems[i];
        String key = item.contains("=") ? item.substring(0, item.indexOf('=')) : item;
        Map<String, Integer> infoKeys =
            metadataKeys.get(INFO) != null ? metadataKeys.get(INFO) : emptyMap();
        Integer metadataId = infoKeys.get(key) != null ? infoKeys.get(key) : -1;
        pstmt.setInt(1, i);
        pstmt.setInt(3, metadataId);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert info field order");
    }
  }
}
