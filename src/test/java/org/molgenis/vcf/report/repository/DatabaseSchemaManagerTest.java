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

        VCFFileReader vcfReader = new VCFFileReader(inputVcfPath, false);
        ReportGeneratorSettings settings = new ReportGeneratorSettings("Test", "v1.0.0", "arg",
                10, metaJson, null, null, decisionTree, sampleTree, config);
        dbSchemaManager.createDatabase(settings, vcfReader.getFileHeader(), conn);

        verify(stmt).execute(VCF_TABLE_SQL);
        verify(stmt).execute(CONFIG_TABLE_SQL);
        verify(stmt).execute(SAMPLE_PHENOTYPE_TABLE_SQL);
        verify(stmt).execute(PHENOTYPE_TABLE_SQL);
        verify(stmt).execute(EXPECTED_SAMPLE_TABLE);
        verify(stmt).execute(DECISION_TREE_TABLE_SQL);
        verify(stmt).execute(EXPECTED_METADATA_TABLE);
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

        dbSchemaManager.createTable("CREATE TABLE test (id INTEGER)", conn);

        verify(stmt).execute("CREATE TABLE test (id INTEGER)");
    }

    @Test
    void testToSqlType_allCases() {
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 1));
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.CATEGORICAL, 1));
        assertEquals("REAL", DatabaseSchemaManager.toSqlType(ValueType.FLOAT, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.CHARACTER, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.FLAG, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 2)); // count != 1 is TEXT
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, null));
    }

    public static final String EXPECTED_INFO_TABLE = "CREATE TABLE info (id INTEGER PRIMARY KEY AUTOINCREMENT,variant_id INTEGER REFERENCES variant(id),AA TEXT,NS INTEGER,AF TEXT,H2 TEXT,DP INTEGER,DB TEXT);";
    public static final String EXPECTED_FORMAT_TABLE = "CREATE TABLE format (id INTEGER PRIMARY KEY AUTOINCREMENT,sample_id INTEGER REFERENCES sample(id),variant_id INTEGER REFERENCES vcf(id),HQ TEXT,GQ INTEGER,DP INTEGER,GT TEXT);";
    public static final String EXPECTED_METADATA_TABLE = """
               CREATE TABLE metadata (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    fieldType TEXT NOT NULL,
                    valueType TEXT NOT NULL,
                    numberType TEXT NOT NULL,
                    numberCount INTEGER,
                    required INTEGER NOT NULL,
                    separator TEXT,
                    categories TEXT,
                    label TEXT,
                    description TEXT,
                    parent TEXT,
                    nested INTEGER,
                    nullValue TEXT,
                    CHECK (
                        fieldType IN ('INFO', 'FORMAT') AND
                        valueType IN ('INTEGER', 'FLOAT', 'FLAG', 'CHARACTER', 'STRING', 'CATEGORICAL') AND
                        numberType IN ('A', 'R', 'G', 'VARIABLE', 'FIXED')
                   )
               );
            """;
    public static final String EXPECTED_SAMPLE_TABLE = """
                CREATE TABLE sample (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    familyId TEXT,
                    individualId TEXT,
                    paternalId TEXT,
                    maternalId TEXT,
                    sex TEXT NOT NULL,
                    affectedStatus TEXT NOT NULL,
                    sample_index INTEGER,
                    proband INTEGER,
                    CHECK (
                        sex IN ('MALE', 'FEMALE', 'UNKNOWN') AND
                        affectedStatus IN ('AFFECTED', 'UNAFFECTED', 'MISSING')
                    )
                );
            """;
}
