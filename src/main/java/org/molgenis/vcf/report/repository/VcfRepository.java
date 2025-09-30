package org.molgenis.vcf.report.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class VcfRepository {

    public static final String MISSING = ".";

    public int insertVariant(Connection conn, VariantContext vc) throws SQLException {
        try (
                PreparedStatement insertVCF = conn.prepareStatement(
                        "INSERT INTO vcf (chrom, pos, id_vcf, ref, alt, qual, filter) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            insertVCF.setString(1, vc.getContig());
            insertVCF.setInt(2, vc.getStart());
            insertVCF.setString(3, writeJsonListValue(vc.getID(), ","));
            insertVCF.setString(4, vc.getReference().getDisplayString());
            insertVCF.setString(5, toJson(vc.getAlternateAlleles().stream().map(Allele::getDisplayString).toList()));
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

    private static String writeJsonListValue(String value, String separator){
        return !value.equals(MISSING) ? toJson(value.split(separator)) : "[]";
    }

    //FIXME: move to utils
    public static String toJson(Object arr) {
        try {
            return new ObjectMapper().writeValueAsString(arr);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }
}
