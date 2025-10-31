package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.molgenis.vcf.report.repository.FormatRepository.VIPC_S;
import static org.molgenis.vcf.report.utils.CategoryUtils.addCategorical;
import static org.molgenis.vcf.report.utils.CategoryUtils.loadCategoriesMap;
import static org.molgenis.vcf.report.utils.JsonUtils.toJson;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;
import static org.molgenis.vcf.utils.metadata.ValueType.FLAG;

@Component
public class InfoRepository {

    public void insertInfoData(
            Connection conn, VariantContext vc,
            List<String> infoColumns,
            FieldMetadatas fieldMetadatas,
            int variantId, boolean hasSampleTree
    ) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);

        try (PreparedStatement insertInfo = prepareInsertInfo(conn, infoColumns)) {
            insertInfo.setInt(1, variantId);
            for (int i = 0; i < infoColumns.size(); i++) {
                insertInfoDataColumn(vc, infoColumns, fieldMetadatas, i, insertInfo, categoryLookup, hasSampleTree);
            }
            insertInfo.executeUpdate();
        }
    }

    private static void insertInfoDataColumn(VariantContext vc, List<String> infoColumns, FieldMetadatas fieldMetadatas, int i, PreparedStatement insertInfo, Map<FieldValueKey, Integer> categoryLookup, boolean hasSampleTree) throws SQLException {
        final String key = infoColumns.get(i);
        final FieldMetadata meta = fieldMetadatas.getInfo().get(key);
        Object value = vc.getAttribute(key, null);

        if(meta.getType() == FLAG) {
            int flagVal = (value == null) ? 0 : 1;
            insertInfo.setInt(i + 2, flagVal);
        } else if(meta.getType() == CATEGORICAL || (VIPC_S.equals(key) && hasSampleTree)) {
            addCategorical(INFO, meta, categoryLookup, key, value, insertInfo, i + 2);
        } else if((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null) {
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

    private PreparedStatement prepareInsertInfo(Connection conn, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO info (variant_id");
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }
}
