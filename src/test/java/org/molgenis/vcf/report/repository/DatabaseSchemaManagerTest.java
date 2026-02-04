package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.molgenis.vcf.report.repository.DatabaseSchemaManager.*;

import htsjdk.variant.vcf.VCFFileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.*;

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
    ReportGeneratorSettings settings =
        new ReportGeneratorSettings(
            "Test",
            "v1.0.0",
            "arg",
            10,
            metaJson,
            wasmPath,
            null,
            null,
            null,
            decisionTree,
            sampleTree,
            config);
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
    verify(stmt).execute(FORMAT_VARIANT_ID_INDEX_SQL);
    verify(stmt).execute(FORMAT_SAMPLE_INDEX_SQL);
    verify(stmt).execute(FORMAT_VARIANT_ID_SAMPLE_INDEX_SQL);
    verify(stmt).execute(VCF_CHROM_INDEX_SQL);
    verify(stmt).execute(VCF_FORMAT_INDEX_SQL);
    verify(stmt).execute(VCF_POS_INDEX_SQL);
    verify(stmt).execute(VCF_CHROM_POS_INDEX_SQL);
    verify(stmt).execute(CONTIG_VALUE_INDEX_SQL);
    verify(stmt).execute(GT_TYPE_VALUE_INDEX_SQL);
    verify(stmt).execute(INFO_VARIANT_ID_INDEX_SQL);
    verify(stmt).execute(FORMAT_GT_TYPE);

    // check INDEX for nested table
    verify(stmt)
        .execute("CREATE INDEX \"idx_variant_CSQ_variantId\" ON \"variant_CSQ\"(\"_variantId\");");
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
    assertEquals(
        "TEXT", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 2)); // count != 1 is TEXT
    assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, null));
  }

  private static final String NESTED_TABLE_SQL =
      "CREATE TABLE \"variant_CSQ\" ("
          + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
          + "\"_variantId\" INTEGER REFERENCES \"vcf\"(\"_id\"), "
          + "\"CsqIndex\" INTEGER, "
          + // if you want it quoted too
          "\"PolyPhen\" TEXT, "
          + "\"SIFT\" TEXT, "
          + "\"SYMBOL_SOURCE\" TEXT, "
          + "\"SpliceAI_pred_DP_AG\" TEXT, "
          + "\"PICK\" TEXT, "
          + "\"CAPICE_CL\" TEXT, "
          + "\"Consequence\" TEXT, "
          + "\"Feature\" TEXT, "
          + "\"VIPC\" TEXT, "
          + "\"Allele\" TEXT, "
          + "\"IMPACT\" TEXT, "
          + "\"SYMBOL\" TEXT, "
          + "\"Gene\" TEXT, "
          + "\"HGNC_ID\" TEXT, "
          + "\"VIPP\" TEXT, "
          + "\"CAPICE_SC\" TEXT, "
          + "\"Feature_type\" TEXT"
          + ") STRICT;";

  private static final String EXPECTED_INFO_TABLE =
      "CREATE TABLE \"info\" ("
          + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "\"_variantId\" INTEGER REFERENCES \"vcf\"(\"_id\"),"
          + "\"AA\" TEXT,"
          + "\"NS\" INTEGER,"
          + "\"AF\" TEXT,"
          + "\"H2\" TEXT,"
          + "\"DP\" INTEGER,"
          + "\"DB\" TEXT"
          + ") STRICT;";

  private static final String EXPECTED_FORMAT_TABLE =
      "CREATE TABLE \"format\" ("
          + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "\"_sampleIndex\" INTEGER REFERENCES \"sample\"(\"sampleIndex\"),"
          + "\"_variantId\" INTEGER REFERENCES \"vcf\"(\"_id\"),"
          + "\"HQ\" TEXT,"
          + "\"GQ\" INTEGER,"
          + "\"DP\" INTEGER,"
          + "\"GT\" TEXT,"
          + "\"_GtType\" INTEGER REFERENCES \"gtType\"(\"id\")"
          + ") STRICT;";
}
