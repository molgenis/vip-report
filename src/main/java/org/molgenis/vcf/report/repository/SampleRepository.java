package org.molgenis.vcf.report.repository;

import lombok.NonNull;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.report.repository.SqlUtils.insertLookupValues;

@Component
public class SampleRepository {

    static final String INSERT_SAMPLE_SQL = """
            INSERT INTO sample (
                sampleIndex,
                familyId,
                individualId,
                paternalId,
                maternalId,
                sex,
                affectedStatus,
                proband
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public void insertSamples(Connection conn, Items<Sample> sampleItems) throws SQLException {

        Map<Object, Integer> sexIds = insertLookupValues(conn, "sex", List.of(Sex.values()));
        Map<Object, Integer> affectedIds = insertLookupValues(conn, "affectedStatus", List.of(AffectedStatus.values()));

        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SAMPLE_SQL)) {
            @NonNull List<Sample> samples = sampleItems.getItems();
            Set<Integer> addedSamples = new HashSet<>();
            for (Sample sample : samples) {
                addedSamples = addSample(sample, samples, pstmt, affectedIds, sexIds, addedSamples);
            }
            pstmt.executeBatch();
        }
    }

    private Set<Integer> addSample(Sample sample, List<Sample> samples, PreparedStatement pstmt, Map<Object, Integer> affectedIds, Map<Object, Integer> sexIds, Set<Integer> addedSamples) throws SQLException {
        Person p = sample.getPerson();
        if(!addedSamples.contains(sample.getIndex())) {
            Sample father = getSample(p.getPaternalId(), samples);
            if (father != null) {
                if (!addedSamples.contains(father.getIndex())) {
                    addedSamples = addSample(father, samples, pstmt, affectedIds, sexIds, addedSamples);
                }
            }
            Sample mother = getSample(p.getMaternalId(), samples);
            if (mother != null) {
                if (!addedSamples.contains(mother.getIndex())) {
                    addedSamples = addSample(mother, samples, pstmt, affectedIds, sexIds, addedSamples);
                }
            }

            pstmt.setInt(1, sample.getIndex());
            pstmt.setString(2, p.getFamilyId());
            pstmt.setString(3, p.getIndividualId());
            if (father != null) {
                pstmt.setInt(4, father.getIndex());
            }
            if (mother != null) {
                pstmt.setInt(5, mother.getIndex());
            }
            pstmt.setInt(6, sexIds.get(p.getSex()));
            pstmt.setInt(7, affectedIds.get(p.getAffectedStatus()));
            pstmt.setInt(8, sample.isProband() ? 1 : 0);
            pstmt.addBatch();
            addedSamples.add(sample.getIndex());
        }
        return addedSamples;
    }

    private Sample getSample(String individualId, List<Sample> samples) {
        if(individualId == null || individualId.equals("0")) {
            return null;
        }
        for(Sample sample : samples) {
            if(sample.getPerson().getIndividualId().equals(individualId)) {
                return sample;
            }
        }
        throw new MissingSampleException(individualId);
    }
}
