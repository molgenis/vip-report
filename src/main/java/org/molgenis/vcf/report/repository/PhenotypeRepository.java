package org.molgenis.vcf.report.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class PhenotypeRepository {

  public void insertPhenotypeData(
      Connection conn, List<Phenopacket> packets, List<Sample> samples) {
    String phenotypeSql = "INSERT OR IGNORE INTO phenotype (id, label) VALUES (?, ?)";
    String samplePhenoSql = "INSERT INTO samplePhenotype (sampleIndex, phenotypeId) VALUES (?, ?)";

    try (PreparedStatement phenotypeStmt = conn.prepareStatement(phenotypeSql);
        PreparedStatement samplePhenoStmt = conn.prepareStatement(samplePhenoSql)) {

      for (Phenopacket packet : packets) {
        List<Sample> matchedSamples =
            samples.stream()
                .filter(s -> s.getPerson().getIndividualId().equals(packet.getSubject().getId()))
                .toList();
        if (matchedSamples.size() == 1) {
          Sample sample = matchedSamples.getFirst();

          for (PhenotypicFeature feature : packet.getPhenotypicFeaturesList()) {
            String phenotypeId = feature.getOntologyClass().getId();
            String phenotypeLabel = feature.getOntologyClass().getLabel();

            phenotypeStmt.setString(1, phenotypeId);
            phenotypeStmt.setString(2, phenotypeLabel);
            phenotypeStmt.addBatch();

            samplePhenoStmt.setInt(1, sample.getIndex());
            samplePhenoStmt.setString(2, phenotypeId);
            samplePhenoStmt.addBatch();
          }
        }
        phenotypeStmt.executeBatch();
        samplePhenoStmt.executeBatch();
      }
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage(), "insert phenotype data");
    }
  }
}
