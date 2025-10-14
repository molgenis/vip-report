package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.molgenis.vcf.report.utils.CategoryUtils.addCategorical;
import static org.molgenis.vcf.report.utils.CategoryUtils.loadCategoriesMap;
import static org.molgenis.vcf.report.utils.JsonUtils.toJson;
import static org.molgenis.vcf.utils.metadata.FieldType.FORMAT;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL;


@Component
public class FormatRepository {

    public static final String GT_TYPE = "GT_type";
    static final String VIPC_S = "VIPC_S";

    private static String getOriginalGTString(Genotype genotype, VariantContext variantContext) {
        StringBuilder gtString = new StringBuilder();
        String sep = genotype.isPhased() ? "|" : "/";

        for(int i = 0; i < genotype.getPloidy(); i++) {
            if(i > 0) gtString.append(sep);
            Allele allele = genotype.getAllele(i);
            int alleleIndex = variantContext.getAlleleIndex(allele);
            gtString.append(alleleIndex);
        }
        return gtString.toString();
    }

    public void insertFormatData(Connection conn, VariantContext vc, List<String> formatColumns, int variantId, FieldMetadatas fieldMetadatas, List<Sample> samples,
                                 boolean hasSampleTree) throws SQLException {
        Map<FieldValueKey, Integer> categoryLookup = loadCategoriesMap(conn);
        try (PreparedStatement insertFormat = prepareInsertFormat(conn, formatColumns)) {
            insertFormat.setInt(1, variantId);
            for (Genotype genotype : vc.getGenotypes()) {
                Sample sample = samples.stream().filter(s -> s.getPerson().getIndividualId()
                        .equals(genotype.getSampleName())).toList().getFirst();
                int sampleId = sample.getIndex();
                insertFormat.setInt(2, sampleId);
                for (int i = 0; i < formatColumns.size(); i++) {
                    if(formatColumns.get(i).equals(GT_TYPE)){
                        insertFormat.setString(i + 3, genotype.getType().toString());
                    }else {
                        insertFormatDataColumn(vc, formatColumns, fieldMetadatas, genotype, i, categoryLookup, insertFormat, hasSampleTree);
                    }
                }
                insertFormat.addBatch();
            }
            insertFormat.executeBatch();
        }
    }

    private static void insertFormatDataColumn(VariantContext vc, List<String> formatColumns, FieldMetadatas fieldMetadatas,
                                               Genotype genotype, int i, Map<FieldValueKey, Integer> categoryLookup, PreparedStatement insertFormat,
                                               boolean hasSampleTree
    ) throws SQLException {

        final String key = formatColumns.get(i);
        final FieldMetadata meta = fieldMetadatas.getFormat().get(key);
        Object value = genotype.hasAnyAttribute(key) ? genotype.getAnyAttribute(key) : null;

        if(meta.getType() == CATEGORICAL || (key.equals(VIPC_S) && hasSampleTree)) {
            addCategorical(FORMAT, meta, categoryLookup, key, value, insertFormat, i + 3);
        } else {
            value = getFormatValue(vc, genotype, meta, value, key);
            String dbValue;
            if(value instanceof Iterable<?>) {
                dbValue = toJson(value);
            } else {
                dbValue = value != null ? value.toString() : null;
            }
            insertFormat.setString(i + 3, dbValue);
        }
    }

    private static Object getFormatValue(VariantContext vc, Genotype genotype, FieldMetadata meta, Object value, String key) {
        if((meta.getNumberType() != FIXED || meta.getNumberCount() != 1) && value != null && !(value instanceof Iterable<?>)) {
            String separator = meta.getSeparator() != null ? meta.getSeparator().toString() : ",";
            value = List.of(value.toString().split(separator));
        }
        if(value != null && "GT".equals(key)) {
            value = getOriginalGTString(genotype, vc);
        }
        return value;
    }


    private PreparedStatement prepareInsertFormat(Connection conn, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO format (variant_id, sample_id");
        for (String column : columns) {
            sql.append(", ").append(column);
        }
        sql.append(") VALUES (?, ?");
        sql.append(", ?".repeat(columns.size()));
        sql.append(")");
        return conn.prepareStatement(sql.toString());
    }
}
