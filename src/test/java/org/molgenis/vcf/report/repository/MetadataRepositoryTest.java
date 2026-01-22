package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.*;
import static org.molgenis.vcf.report.repository.MetadataRepository.INSERT_CATEGORIES_SQL;
import static org.molgenis.vcf.report.repository.MetadataRepository.INSERT_METADATA_SQL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.HpoTerm;
import org.molgenis.vcf.utils.metadata.ValueCount;
import org.molgenis.vcf.utils.metadata.ValueCount.Type;
import org.molgenis.vcf.utils.metadata.ValueType;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Individual;
import org.molgenis.vcf.utils.sample.model.OntologyClass;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.PhenotypicFeature;

@ExtendWith(MockitoExtension.class)
class MetadataRepositoryTest {
  private Connection conn;
  private MetadataRepository repository;

  @BeforeEach
  void setUp() {
    conn = mock(Connection.class);
    repository = new MetadataRepository();
  }

  @Test
  void testInsertMetadata() throws Exception {
    PreparedStatement preparedMetadata = mock(PreparedStatement.class);
    PreparedStatement preparedCategories = mock(PreparedStatement.class);
    PreparedStatement preparedNumberType = mock(PreparedStatement.class);
    PreparedStatement preparedFieldTypes = mock(PreparedStatement.class);
    PreparedStatement preparedValueTypes = mock(PreparedStatement.class);

    when(conn.prepareStatement("INSERT INTO numberType (id, value) VALUES (?, ?)"))
        .thenReturn(preparedNumberType);
    when(conn.prepareStatement("INSERT INTO fieldType (id, value) VALUES (?, ?)"))
        .thenReturn(preparedFieldTypes);
    when(conn.prepareStatement("INSERT INTO valueType (id, value) VALUES (?, ?)"))
        .thenReturn(preparedValueTypes);
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true);
    when(preparedMetadata.getGeneratedKeys()).thenReturn(resultSet);
    when(conn.prepareStatement(INSERT_METADATA_SQL, Statement.RETURN_GENERATED_KEYS))
        .thenReturn(preparedMetadata);
    when(conn.prepareStatement(INSERT_CATEGORIES_SQL)).thenReturn(preparedCategories);

    FieldMetadata vipcSFieldMeta = mock(FieldMetadata.class, RETURNS_DEEP_STUBS);
    when(vipcSFieldMeta.getType()).thenReturn(ValueType.CATEGORICAL);
    when(vipcSFieldMeta.getNumberType()).thenReturn(ValueCount.Type.FIXED);
    when(vipcSFieldMeta.isRequired()).thenReturn(true);
    when(vipcSFieldMeta.getSeparator()).thenReturn(null);
    when(vipcSFieldMeta.getNumberCount()).thenReturn(1);
    when(vipcSFieldMeta.getLabel()).thenReturn("Test");
    when(vipcSFieldMeta.getDescription()).thenReturn("Test Desc");
    when(vipcSFieldMeta.getCategories())
        .thenReturn(Map.of("U1", new ValueDescription("U1", "Desc")));

    FieldMetadata hpoFieldMeta = mock(FieldMetadata.class, RETURNS_DEEP_STUBS);
    when(hpoFieldMeta.getType()).thenReturn(ValueType.CATEGORICAL);
    when(hpoFieldMeta.getNumberType()).thenReturn(Type.VARIABLE);
    when(hpoFieldMeta.isRequired()).thenReturn(false);
    when(hpoFieldMeta.getSeparator()).thenReturn(';');
    when(hpoFieldMeta.getLabel()).thenReturn("HPO");
    when(hpoFieldMeta.getDescription()).thenReturn("HPO Desc");

    Map<String, FieldMetadata> formatFields = Map.of("VIPC_S", vipcSFieldMeta);
    Map<String, FieldMetadata> infoFields = Map.of("HPO", hpoFieldMeta);
    Map<String, HpoTerm> hpoTerms = new HashMap<>();
    hpoTerms.put("HPO term", new HpoTerm("HPO term", "HPO label", "HPO desc"));

    FieldMetadatas metadatas = mock(FieldMetadatas.class);
    when(metadatas.getFormat()).thenReturn(formatFields);
    when(metadatas.getInfo()).thenReturn(infoFields);

    PhenotypicFeature phenotypicFeature = mock(PhenotypicFeature.class);
    when(phenotypicFeature.getOntologyClass()).thenReturn(new OntologyClass("HPO term", "MyLabel"));
    Phenopacket phenopacket = new Phenopacket(List.of(phenotypicFeature), new Individual("sample"));
    List<Phenopacket> phenopackets = List.of(phenopacket);
    Path decisionTreePath = Paths.get("src", "test", "resources", "tree.json");
    Path sampleTreePath = Paths.get("src", "test", "resources", "tree.json");

    // collectNodes and collectHpos use file IO. You might want to stub them with static mocking or
    // refactor to accept injected results for true isolation. This test expects static methods that
    // won't actually read a file.

    doReturn(new int[] {1}).when(preparedMetadata).executeBatch();
    doReturn(new int[] {1}).when(preparedNumberType).executeBatch();

    repository.insertMetadata(
        conn, metadatas, decisionTreePath, sampleTreePath, phenopackets, hpoTerms);

    verify(conn).prepareStatement(INSERT_METADATA_SQL, Statement.RETURN_GENERATED_KEYS);
    verify(preparedMetadata, times(2)).executeUpdate();
    verify(preparedCategories).setString(3, "HPO label");
    verify(preparedCategories).setString(4, "HPO desc (HPO term)");
  }

  @Test
  void testInsertHeaderLine() throws Exception {
    PreparedStatement preparedStmt = mock(PreparedStatement.class);
    when(conn.prepareStatement("INSERT INTO header (line) VALUES (?)")).thenReturn(preparedStmt);

    List<String> lines = List.of("FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
    String header = "fileformat=VCFv4.2";
    doNothing().when(preparedStmt).addBatch();
    doReturn(new int[] {1}).when(preparedStmt).executeBatch();

    repository.insertHeaderLine(conn, lines, header);

    verify(conn).prepareStatement(anyString());
    verify(preparedStmt, times(2)).addBatch();
    verify(preparedStmt).executeBatch();
  }
}
