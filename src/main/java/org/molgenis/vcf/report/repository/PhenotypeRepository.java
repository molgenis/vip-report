package org.molgenis.vcf.report.repository;

import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;
import org.molgenis.vcf.utils.sample.model.Phenopacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PhenotypeRepository {

    private final Connection conn;

    public PhenotypeRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertPhenotypeData(ReportData reportData) {
        String phenotypeSql = "INSERT OR IGNORE INTO phenotype (id, label) VALUES (?, ?)";
        String samplePhenoSql = "INSERT OR IGNORE INTO samplePhenotype (sample_id, phenotype_id) VALUES (?, ?)";

        try (PreparedStatement phenotypeStmt = conn.prepareStatement(phenotypeSql);
             PreparedStatement samplePhenoStmt = conn.prepareStatement(samplePhenoSql)) {

            List<?> packets = reportData.getPhenopackets();

            for (Object o : packets) {
                Phenopacket packet = (Phenopacket) o;
                String sampleId = packet.getSubject().getId();

                for (PhenotypicFeature feature : packet.getPhenotypicFeaturesList()) {
                    String phenotypeId = feature.getOntologyClass().getId();
                    String phenotypeLabel = feature.getOntologyClass().getLabel();

                    phenotypeStmt.setString(1, phenotypeId);
                    phenotypeStmt.setString(2, phenotypeLabel);
                    phenotypeStmt.addBatch();

                    samplePhenoStmt.setString(1, sampleId);
                    samplePhenoStmt.setString(2, phenotypeId);
                    samplePhenoStmt.addBatch();
                }
            }
            phenotypeStmt.executeBatch();
            samplePhenoStmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting phenotype data", e);
        }
    }
}
