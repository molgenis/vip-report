package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.metadata.ValueCount;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.sql.*;
import java.util.*;

import static org.molgenis.vcf.report.repository.DatabaseManager.VARIANT_ID;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;
import static org.molgenis.vcf.utils.metadata.ValueType.FLAG;

public class VcfRepository {

    private final Connection conn;

    public VcfRepository(Connection conn) {
        this.conn = conn;
    }

    public int insertVariant(VariantContext vc) throws SQLException {
        try (PreparedStatement insertVCF = conn.prepareStatement(
                "INSERT INTO vcf (chrom, pos, id_vcf, ref, alt, qual, filter) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ObjectMapper objectMapper = new ObjectMapper();
            insertVCF.setString(1, vc.getContig());
            insertVCF.setInt(2, vc.getStart());
            insertVCF.setString(3, objectMapper.writeValueAsString(vc.getID().split(",")));
            insertVCF.setString(4, vc.getReference().getDisplayString());
            insertVCF.setString(5, objectMapper.writeValueAsString(vc.getAlternateAlleles().stream().map(Allele::getDisplayString).toList()));
            insertVCF.setDouble(6, vc.hasLog10PError() ? vc.getPhredScaledQual() : 0.0);
            insertVCF.setString(7, vc.isFiltered() ?objectMapper.writeValueAsString(vc.getFilters()) : objectMapper.writeValueAsString(Set.of("PASS")));

            insertVCF.executeUpdate();

            // Retrieve generated variant_id
            try (ResultSet rs = insertVCF.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve variant_id from vcf insert.");
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); //FIXME
        }
    }

    public void insertCsqData(VariantContext vc, List<String> matchingCsqFields,
                              FieldMetadatas fieldMetadatas, int variantId) throws SQLException {
        Map<String, NestedFieldMetadata> metas = new HashMap();
        for(Map.Entry<String, NestedFieldMetadata> entry : fieldMetadatas.getInfo().get("CSQ").getNestedFields().entrySet()){
            metas.put(entry.getKey(), entry.getValue());
        }
        if (vc.hasAttribute("CSQ")) {
            try (PreparedStatement insertCSQ = prepareInsertSQL("variant_CSQ", matchingCsqFields)) {
                String[] csqEntries = vc.getAttributeAsString("CSQ", "").split(",");
                for (String csq : csqEntries) {
                    String[] values = csq.split("\\|", -1);
                    insertCSQ.setInt(1, variantId);
                    for (int i = 0; i < matchingCsqFields.size(); i++) {
                        String csqField = matchingCsqFields.get(i);
                        NestedFieldMetadata meta = metas.get(csqField);
                        int csqIndex = meta.getIndex();
                        String val = (csqIndex >= 0 && csqIndex < values.length) ? values[csqIndex] : null;
                        if(meta.getSeparator() != null){
                            String[] split = val.split(meta.getSeparator().toString());
                            ObjectMapper objectMapper = new ObjectMapper();
                            val = objectMapper.writeValueAsString(split);
                        }
                        insertCSQ.setString(i + 2, val);
                    }
                    insertCSQ.addBatch();
                }
                insertCSQ.executeBatch();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);//FIXME
            }
        }
    }

    public static String getOriginalGTString(Genotype genotype, VariantContext variantContext) {
        StringBuilder gtString = new StringBuilder();
        String sep = genotype.isPhased() ? "|" : "/";

        for (int i = 0; i < genotype.getPloidy(); i++) {
            if (i > 0) gtString.append(sep);
            Allele allele = genotype.getAllele(i);
            int alleleIndex = variantContext.getAlleleIndex(allele);
            gtString.append(alleleIndex);
        }
        return gtString.toString();
    }

    public void insertFormatData(VariantContext vc, List<String> formatColumns, int variantId, FieldMetadatas fieldMetadatas, List<Sample> samples) throws SQLException {
        try (PreparedStatement insertFormat = prepareInsertFormat(formatColumns)) {
            insertFormat.setInt(1, variantId);
            for (Genotype genotype : vc.getGenotypes()) {
                Sample sample = samples.stream().filter(s -> s.getPerson().getIndividualId()
                        .equals(genotype.getSampleName())).toList().getFirst();
                int sampleId = sample.getIndex();
                insertFormat.setInt(2, sampleId);
                for (int i = 0; i < formatColumns.size(); i++) {
                    String key = formatColumns.get(i);
                    FieldMetadata fieldMetadata = fieldMetadatas.getFormat().get(key);
                    Object value = genotype.hasAnyAttribute(key) ? genotype.getAnyAttribute(key) : null;
                    if(fieldMetadata.getNumberType() != FIXED || fieldMetadata.getNumberCount() != 1) {
                        if(value != null &&!(value instanceof Iterable<?>)){
                            String separator = fieldMetadata.getSeparator() != null ?  fieldMetadata.getSeparator().toString() : ",";
                            value = List.of(value.toString().split(separator));
                        }
                    }
                    if (value != null && "GT".equals(key)) {
                        value = getOriginalGTString(genotype, vc);
                    }
                    if(value instanceof Iterable<?>){
                        ObjectMapper mapper = new ObjectMapper();
                        insertFormat.setString(i + 3, value != null ? mapper.writeValueAsString(value) : null);
                    }else {
                        insertFormat.setString(i + 3, value != null ? value.toString() : null);
                    }
                }
                insertFormat.addBatch();
            }
            insertFormat.executeBatch();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertInfoData(VariantContext vc, List<String> infoColumns,
                               FieldMetadatas fieldMetadatas, int variantId) throws SQLException {
        try (PreparedStatement insertInfo = prepareInsertInfo(infoColumns)) {
            insertInfo.setInt(1, variantId);
            for (int i = 0; i < infoColumns.size(); i++) {
                String key = infoColumns.get(i);
                FieldMetadata meta = fieldMetadatas.getInfo().get(key);
                String value = vc.getAttributeAsString(key, "");
                if(meta.getType() == FLAG){
                    if(value == null){
                        value = "0";
                    }else{
                        value = "1";
                    }
                }
                else if(!(meta.getNumberType() == FIXED && meta.getNumberCount() == 1)){
                    String separator = meta.getSeparator() == null ? ",":meta.getSeparator().toString();
                    String[] split = value.split(separator);
                    ObjectMapper objectMapper = new ObjectMapper();
                    value = objectMapper.writeValueAsString(split);
                }
                insertInfo.setString(i + 2, value != null ? value.toString() : null);
            }
            insertInfo.executeUpdate();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);//FIXME
        }
    }

    private PreparedStatement prepareInsertSQL(String table, List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (").append(VARIANT_ID);
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }

    private PreparedStatement prepareInsertFormat(List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO format (variant_id, sample_id");
        for (String column : columns) {
            sql.append(", ").append(column);
        }
        sql.append(") VALUES (?, ?");
        sql.append(", ?".repeat(columns.size()));
        sql.append(")");
        return conn.prepareStatement(sql.toString());
    }

    private PreparedStatement prepareInsertInfo(List<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO info (variant_id");
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?").append(", ?".repeat(columns.size())).append(")");
        return conn.prepareStatement(sql.toString());
    }

    public List<String> getDatabaseCSQColumns() throws SQLException {
        return getTableColumnsExcluding("variant_CSQ", "id", VARIANT_ID);
    }

    public List<String> getDatabaseFormatColumns() throws SQLException {
        return getTableColumnsExcluding("format", "id", "sample_id", VARIANT_ID);
    }

    public List<String> getDatabaseInfoColumns() throws SQLException {
        return getTableColumnsExcluding("info", "id", VARIANT_ID);
    }

    private List<String> getTableColumnsExcluding(String table, String... excludeColumns) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String col = rs.getString("name");
                boolean excluded = false;
                for (String exc : excludeColumns) {
                    if (col.equalsIgnoreCase(exc)) {
                        excluded = true;
                        break;
                    }
                }
                if (!excluded) {
                    columns.add(col);
                }
            }
        }
        return columns;
    }
}
