package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.utils.sample.model.Individual;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;
import org.molgenis.vcf.utils.sample.model.Sample;

@ExtendWith(MockitoExtension.class)
class PhenotypeRepositoryTest {

  private Connection conn;
  private PreparedStatement phenotypeStmt;
  private PreparedStatement samplePhenoStmt;
  private PhenotypeRepository repo;

  @BeforeEach
  void setUp() throws Exception {
    conn = mock(Connection.class);
    phenotypeStmt = mock(PreparedStatement.class);
    samplePhenoStmt = mock(PreparedStatement.class);
    when(conn.prepareStatement(contains("phenotype"))).thenReturn(phenotypeStmt);
    when(conn.prepareStatement(contains("samplePhenotype"))).thenReturn(samplePhenoStmt);
    repo = new PhenotypeRepository();
  }

  @Test
  void testInsertPhenotypeData() throws SQLException {
    // Mocks for phenotype features and packets
    PhenotypicFeature feature1 = mock(PhenotypicFeature.class);
    PhenotypicFeature feature2 = mock(PhenotypicFeature.class);
    var ontologyClass1 = mock(org.molgenis.vcf.utils.sample.model.OntologyClass.class);
    var ontologyClass2 = mock(org.molgenis.vcf.utils.sample.model.OntologyClass.class);

    when(feature1.getOntologyClass()).thenReturn(ontologyClass1);
    when(feature2.getOntologyClass()).thenReturn(ontologyClass2);
    when(ontologyClass1.getId()).thenReturn("HPO:0001");
    when(ontologyClass1.getLabel()).thenReturn("feature1");
    when(ontologyClass2.getId()).thenReturn("HPO:0002");
    when(ontologyClass2.getLabel()).thenReturn("feature2");

    Phenopacket packet = mock(Phenopacket.class);
    Individual person = mock(Individual.class);
    when(packet.getPhenotypicFeaturesList()).thenReturn(List.of(feature1, feature2));
    when(packet.getSubject()).thenReturn(person);
    when(person.getId()).thenReturn("INDIVIDUAL1");

    Sample sample = mock(Sample.class);
    org.molgenis.vcf.utils.sample.model.Person samplePerson = mock(
        org.molgenis.vcf.utils.sample.model.Person.class);
    when(sample.getPerson()).thenReturn(samplePerson);
    when(samplePerson.getIndividualId()).thenReturn("INDIVIDUAL1");
    when(sample.getIndex()).thenReturn(42);

    doNothing().when(phenotypeStmt).addBatch();
    doReturn(new int[]{1}).when(phenotypeStmt).executeBatch();
    doNothing().when(phenotypeStmt).setString(anyInt(), anyString());
    doNothing().when(samplePhenoStmt).addBatch();
    doReturn(new int[]{1}).when(samplePhenoStmt).executeBatch();
    doNothing().when(samplePhenoStmt).setInt(anyInt(), anyInt());
    doNothing().when(samplePhenoStmt).setString(anyInt(), anyString());

    repo.insertPhenotypeData(conn, List.of(packet), List.of(sample));

    verify(phenotypeStmt).setString(1, "HPO:0001");
    verify(phenotypeStmt).setString(2, "feature1");
    verify(phenotypeStmt).setString(1, "HPO:0002");
    verify(phenotypeStmt).setString(2, "feature2");
    verify(phenotypeStmt, times(2)).addBatch();
    verify(phenotypeStmt, times(1)).executeBatch();

    verify(samplePhenoStmt, times(2)).setInt(1, 42);
    verify(samplePhenoStmt).setString(2, "HPO:0001");
    verify(samplePhenoStmt).setString(2, "HPO:0002");
    verify(samplePhenoStmt, times(2)).addBatch();
    verify(samplePhenoStmt, times(1)).executeBatch();
  }
}
