package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

import static org.molgenis.vcf.report.utils.JsonUtils.toJson;

@Component
public class VcfRepository {

    public static final String MISSING = ".";

    public int insertVariant(Connection conn, VariantContext vc, Map<Object, Integer> contigIds) throws SQLException {

        try (
                PreparedStatement insertVCF = conn.prepareStatement(
                        "INSERT INTO vcf (chrom, pos, id_vcf, ref, alt, qual, filter) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            insertVCF.setInt(1, contigIds.get(vc.getContig()));
            insertVCF.setInt(2, vc.getStart());
            insertVCF.setString(3, writeJsonListValue(vc.getID()));
            insertVCF.setString(4, vc.getReference().getDisplayString());
            insertVCF.setString(5, toJson(vc.getAlternateAlleles().stream().map(this::getDisplayString).toList()));
            if(vc.hasLog10PError()) {
                insertVCF.setDouble(6, vc.getPhredScaledQual());
            }
            if(vc.filtersWereApplied()) {
                insertVCF.setString(7, toJson(vc.isNotFiltered() ? List.of("PASS") : String.join(",", vc.getFilters())));
            }
            insertVCF.executeUpdate();

            try (ResultSet rs = insertVCF.getGeneratedKeys()) {
                if(rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve variant_id from vcf insert.");
                }
            }
        }
    }

    private String getDisplayString(Allele allele) {
        return allele.getDisplayString().contains("<CNV:TR") ? allele.getDisplayString().replaceAll("<CNV:TR\\d+>", "<CNV:TR>") : allele.getDisplayString();
    }

    private static String writeJsonListValue(String value){
        return !value.equals(MISSING) ? toJson(value.split(",")) : "[]";
    }
}
