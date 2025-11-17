package org.molgenis.vcf.report.repository;

import static htsjdk.variant.variantcontext.GenotypeType.HET;
import static htsjdk.variant.variantcontext.GenotypeType.HOM_REF;
import static htsjdk.variant.variantcontext.GenotypeType.HOM_VAR;
import static htsjdk.variant.variantcontext.GenotypeType.MIXED;
import static htsjdk.variant.variantcontext.GenotypeType.NO_CALL;
import static htsjdk.variant.variantcontext.GenotypeType.UNAVAILABLE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.HtsFile;
import org.molgenis.vcf.utils.sample.model.Individual;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;
import org.molgenis.vcf.utils.sample.model.Sample;

@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {

  @Mock VcfRepository vcfRepo;
  @Mock InfoRepository infoRepo;
  @Mock NestedRepository nestedRepo;
  @Mock FormatRepository formatRepo;
  @Mock PhenotypeRepository phenotypeRepo;
  @Mock MetadataRepository metadataRepo;
  @Mock ConfigRepository configRepo;
  @Mock DecisionTreeRepository decisionTreeRepo;
  @Mock SampleRepository sampleRepo;
  @Mock ReportMetadataRepository reportMetadataRepo;

  @Test
  @SuppressWarnings("unchecked")
  void testPopulateDbCoordinatesRepositoriesAndCommit() throws Exception {
    DatabaseManager manager =
        new DatabaseManager(
            vcfRepo,
            infoRepo,
            nestedRepo,
            formatRepo,
            phenotypeRepo,
            metadataRepo,
            configRepo,
            decisionTreeRepo,
            sampleRepo,
            reportMetadataRepo);
    Connection conn = mock(Connection.class);
    Statement stmt = mock(Statement.class);
    when(conn.createStatement()).thenReturn(stmt);
    ResultSet result = mock(ResultSet.class);
    when(result.next()).thenReturn(false);
    when(stmt.executeQuery(anyString())).thenReturn(result);
    doNothing().when(conn).setAutoCommit(false);
    doNothing().when(conn).commit();

    PreparedStatement pstm = mock(PreparedStatement.class);
    when(conn.prepareStatement(any())).thenReturn(pstm);

    Items samples = mock(Items.class);
    Sample sample = mock(Sample.class);
    Person person = mock(Person.class);
    when(person.getIndividualId()).thenReturn("NA00001");
    when(sample.getPerson()).thenReturn(person);
    List<Sample> sampleList = List.of(sample);
    when(samples.getItems()).thenReturn(sampleList);

    FieldMetadatas fieldMetadatas = mock(FieldMetadatas.class);
    when(fieldMetadatas.getInfo()).thenReturn(Collections.emptyMap());
    HtsFile htsFile = mock(HtsFile.class);
    ReportMetadata reportMetadata =
        new ReportMetadata(new AppMetadata("report", "v1", "args"), htsFile);
    Map<Object, Object> config = new HashMap<>();
    Individual individual = mock(Individual.class);
    PhenotypicFeature phenotypicFeature = mock(PhenotypicFeature.class);
    Phenopacket phenopacket = new Phenopacket(List.of(phenotypicFeature), individual);
    List<Phenopacket> phenopackets = List.of(phenopacket);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example_fixed.vcf");
    Path exampleDb = Paths.get("src", "test", "resources", "example.db");
    Path decisionTree = Paths.get("src", "test", "resources", "tree.json");
    Path sampleTree = Paths.get("src", "test", "resources", "tree.json");

    when(metadataRepo.insertMetadata(any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyMap());

    manager.setConnection(conn);
    manager.populateDb(
        exampleDb.toString(),
        fieldMetadatas,
        samples,
        inputVcfPath.toFile(),
        decisionTree,
        sampleTree,
        reportMetadata,
        config,
        phenopackets);

    verify(vcfRepo).insertVariant(eq(conn), any(), any(), anyInt());
    verify(infoRepo)
        .insertInfoData(eq(conn), any(), eq(List.of()), eq(fieldMetadatas), eq(0), eq(true));
    verify(formatRepo)
        .insertFormatData(
            eq(conn),
            any(),
            eq(List.of()),
            eq(0),
            eq(fieldMetadatas),
            eq(sampleList),
            eq(true),
            eq(Map.of(UNAVAILABLE, 4, HOM_REF, 1, HET, 2, HOM_VAR, 3, MIXED, 5, NO_CALL, 0)));
    verify(phenotypeRepo).insertPhenotypeData(conn, phenopackets, sampleList);
    verify(metadataRepo)
        .insertMetadata(conn, fieldMetadatas, decisionTree, sampleTree, phenopackets);
    verify(configRepo).insertConfigData(conn, Map.of());
    verify(decisionTreeRepo).insertDecisionTreeData(conn, decisionTree, sampleTree);
    verify(sampleRepo).insertSamples(conn, samples);
    verify(reportMetadataRepo).insertReportMetadata(conn, reportMetadata);
    verify(conn).setAutoCommit(false);
    verify(conn).commit();
  }
}
