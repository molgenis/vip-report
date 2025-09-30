package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.utils.model.ValueDescription;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Phenopacket;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataRepositoryTest {

    private Connection conn;
    private PreparedStatement preparedMetadata;
    private PreparedStatement preparedCategories;
    private MetadataRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        conn = mock(Connection.class);
        preparedMetadata = mock(PreparedStatement.class);
        preparedCategories = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(preparedMetadata, preparedCategories);
        repository = new MetadataRepository();
    }

    @Test
    void testInsertMetadata() throws Exception {
        FieldMetadata fieldMeta = mock(FieldMetadata.class, RETURNS_DEEP_STUBS);
        when(fieldMeta.getType().name()).thenReturn("CATEGORICAL");
        when(fieldMeta.getNumberType().name()).thenReturn("FIXED");
        when(fieldMeta.isRequired()).thenReturn(true);
        when(fieldMeta.getSeparator()).thenReturn(null);
        when(fieldMeta.getNumberCount()).thenReturn(1);
        when(fieldMeta.getLabel()).thenReturn("Test");
        when(fieldMeta.getDescription()).thenReturn("Test Desc");
        when(fieldMeta.getCategories()).thenReturn(Map.of("U1", new ValueDescription("U1", "Desc")));

        Map<String, FieldMetadata> formatFields = Map.of("U1", fieldMeta);
        Map<String, FieldMetadata> infoFields = Map.of();

        FieldMetadatas metadatas = mock(FieldMetadatas.class);
        when(metadatas.getFormat()).thenReturn(formatFields);
        when(metadatas.getInfo()).thenReturn(infoFields);

        List<Phenopacket> phenopackets = List.of();
        Path decisionTreePath = Paths.get("src", "test", "resources", "tree.json");
        Path sampleTreePath = Paths.get("src", "test", "resources", "tree.json");

        // collectNodes and collectHpos use file IO. You might want to stub them with static mocking or refactor to accept injected results for true isolation. This test expects static methods that won't actually read a file.

        doNothing().when(preparedMetadata).addBatch();
        doReturn(new int[]{1}).when(preparedMetadata).executeBatch();
        doNothing().when(preparedCategories).addBatch();
        doReturn(new int[]{1}).when(preparedCategories).executeBatch();

        repository.insertMetadata(conn, metadatas, decisionTreePath, sampleTreePath, phenopackets);

        verify(conn).prepareStatement("""
            INSERT INTO metadata (
                name,
                fieldType,
                valueType,
                numberType,
                numberCount,
                required,
                separator,
                categories,
                label,
                description,
                parent,
                nested,
                nullValue
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """);
        verify(preparedMetadata).addBatch();
        verify(preparedMetadata).executeBatch();
    }

    @Test
    void testInsertHeaderLine() throws Exception {
        List<String> lines = List.of("FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
        String header = "fileformat=VCFv4.2";
        doNothing().when(preparedMetadata).addBatch();
        doReturn(new int[]{1}).when(preparedMetadata).executeBatch();

        repository.insertHeaderLine(conn, lines, header);

        verify(conn).prepareStatement(anyString());
        verify(preparedMetadata, times(2)).addBatch();
        verify(preparedMetadata).executeBatch();
    }
}
