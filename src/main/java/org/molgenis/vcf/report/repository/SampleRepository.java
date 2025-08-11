package org.molgenis.vcf.report.repository;

import lombok.NonNull;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SampleRepository {

    private final Connection conn;

    public SampleRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertSamples(Items sampleItems) throws SQLException {

        String sql = """
                INSERT INTO sample (
                    id,
                    familyId,
                    individualId,
                    paternalId,
                    maternalId,
                    sex,
                    affectedStatus,
                    sample_index,
                    proband
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            @NonNull List samples = sampleItems.getItems();
            for (Object sampleObject : samples) {
                Sample sample = (Sample) sampleObject;
                Person p = sample.getPerson();
                pstmt.setString(1, String.format("%s_%s", p.getFamilyId(), p.getIndividualId()));
                pstmt.setString(2, p.getFamilyId());
                pstmt.setString(3, p.getIndividualId());
                pstmt.setString(4, p.getPaternalId());
                pstmt.setString(5, p.getMaternalId());
                pstmt.setString(6, p.getSex().name());
                pstmt.setString(7, p.getAffectedStatus().name());
                pstmt.setInt(8, sample.getIndex());
                pstmt.setInt(9, sample.isProband() ? 1 : 0);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
