package org.molgenis.vcf.report.repository;

import htsjdk.variant.vcf.VCFFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.molgenis.vcf.report.repository.DatabaseSchemaManager.*;

class DatabaseSchemaManagerTest {
    private DatabaseSchemaManager dbSchemaManager;

    @BeforeEach
    void setup() {
        dbSchemaManager = new DatabaseSchemaManager();
    }

    @Test
    void testCreateDb() throws SQLException {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        when(conn.createStatement()).thenReturn(stmt);

        Path inputVcfPath = Paths.get("src", "test", "resources", "example_fixed.vcf");
        Path metaJson = Paths.get("src", "test", "resources", "minimal_field_metadata.json");
        Path decisionTree = Paths.get("src", "test", "resources", "tree.json");
        Path sampleTree = Paths.get("src", "test", "resources", "tree.json");
        Path config = Paths.get("src", "test", "resources", "template_config.json");
        Path wasmPath = Paths.get("src", "test", "resources", "fake.wasm");

        VCFFileReader vcfReader = new VCFFileReader(inputVcfPath, false);
        ReportGeneratorSettings settings = new ReportGeneratorSettings("Test", "v1.0.0", "arg",
                10, metaJson, wasmPath,null, null, decisionTree, sampleTree, config);
        dbSchemaManager.createDatabase(settings, vcfReader.getFileHeader(), conn);

        verify(stmt).execute(VCF_TABLE_SQL);
        verify(stmt).execute(NESTED_TABLE_SQL);
        verify(stmt).execute(CONFIG_TABLE_SQL);
        verify(stmt).execute(SAMPLE_PHENOTYPE_TABLE_SQL);
        verify(stmt).execute(PHENOTYPE_TABLE_SQL);
        verify(stmt).execute(SAMPLE_TABLE_SQL);
        verify(stmt).execute(DECISION_TREE_TABLE_SQL);
        verify(stmt).execute(METADATA_TABLE_SQL);
        verify(stmt).execute(APP_METADATA_TABLE_SQL);
        verify(stmt).execute(HEADER_TABLE_SQL);
        verify(stmt).execute(EXPECTED_INFO_TABLE);
        verify(stmt).execute(EXPECTED_FORMAT_TABLE);
        verify(stmt).execute(CATEGORIES_TABLE_SQL);
    }

    @Test
    void testCreateTable_executesSQL() throws SQLException {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        when(conn.createStatement()).thenReturn(stmt);

        dbSchemaManager.executeSql("CREATE TABLE test (id INTEGER)", conn);

        verify(stmt).execute("CREATE TABLE test (id INTEGER)");
    }

    @Test
    void testToSqlType_allCases() {
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 1));
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.CATEGORICAL, 1));
        assertEquals("REAL", DatabaseSchemaManager.toSqlType(ValueType.FLOAT, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.CHARACTER, 1));
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.FLAG, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 2)); // count != 1 is TEXT
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, null));
    }

    private static final String NESTED_TABLE_SQL = "CREATE TABLE info (id INTEGER PRIMARY KEY AUTOINCREMENT,variantId INTEGER REFERENCES vcf(id),AA TEXT,NS INTEGER,AF TEXT,H2 TEXT,DP INTEGER,DB TEXT);";
    private static final String EXPECTED_INFO_TABLE = "CREATE TABLE info (id INTEGER PRIMARY KEY AUTOINCREMENT,variantId INTEGER REFERENCES vcf(id),AA TEXT,NS INTEGER,AF TEXT,H2 TEXT,DP INTEGER,DB TEXT);";
    private static final String EXPECTED_FORMAT_TABLE = "CREATE TABLE format (id INTEGER PRIMARY KEY AUTOINCREMENT,sampleIndex INTEGER REFERENCES sample(sampleIndex),variantId INTEGER REFERENCES vcf(id),HQ TEXT,GQ INTEGER,DP INTEGER,GT TEXT,GtType TEXT);";
}
