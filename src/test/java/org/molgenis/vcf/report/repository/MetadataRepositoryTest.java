package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.report.repository.MetadataRepository.INSERT_METADATA_SQL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.utils.metadata.ValueCount;
import org.molgenis.vcf.utils.metadata.ValueType;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Phenopacket;

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

    FieldMetadata fieldMeta = mock(FieldMetadata.class, RETURNS_DEEP_STUBS);
    when(fieldMeta.getType()).thenReturn(ValueType.CATEGORICAL);
    when(fieldMeta.getNumberType()).thenReturn(ValueCount.Type.FIXED);
    when(fieldMeta.isRequired()).thenReturn(true);
    when(fieldMeta.getSeparator()).thenReturn(null);
    when(fieldMeta.getNumberCount()).thenReturn(1);
    when(fieldMeta.getLabel()).thenReturn("Test");
    when(fieldMeta.getDescription()).thenReturn("Test Desc");
    when(fieldMeta.getCategories()).thenReturn(Map.of("U1", new ValueDescription("U1", "Desc")));

    Map<String, FieldMetadata> formatFields = Map.of("VIPC_S", fieldMeta);
    Map<String, FieldMetadata> infoFields = Map.of();

    FieldMetadatas metadatas = mock(FieldMetadatas.class);
    when(metadatas.getFormat()).thenReturn(formatFields);
    when(metadatas.getInfo()).thenReturn(infoFields);

    List<Phenopacket> phenopackets = List.of();
    Path decisionTreePath = Paths.get("src", "test", "resources", "tree.json");
    Path sampleTreePath = Paths.get("src", "test", "resources", "tree.json");

    // collectNodes and collectHpos use file IO. You might want to stub them with static mocking or
    // refactor to accept injected results for true isolation. This test expects static methods that
    // won't actually read a file.

    doReturn(new int[] {1}).when(preparedMetadata).executeBatch();
    doReturn(new int[] {1}).when(preparedNumberType).executeBatch();

    repository.insertMetadata(conn, metadatas, decisionTreePath, sampleTreePath, phenopackets);

    verify(conn).prepareStatement(INSERT_METADATA_SQL, Statement.RETURN_GENERATED_KEYS);
    verify(preparedMetadata).executeUpdate();
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
