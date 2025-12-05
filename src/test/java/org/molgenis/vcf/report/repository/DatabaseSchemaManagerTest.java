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

        verify(stmt).execute("CREATE INDEX idx_format_variantId ON format(_variantId);");
        verify(stmt).execute("CREATE INDEX idx_format_sampleIndex ON format(_sampleIndex);");
        verify(stmt).execute("CREATE INDEX idx_format_variantId_sampleIndex ON format(_variantId,_sampleIndex);");
        verify(stmt).execute("CREATE INDEX idx_format_GtType ON format(_GtType);");
        verify(stmt).execute("CREATE INDEX idx_vcf_chrom ON vcf(chrom);");
        verify(stmt).execute("CREATE INDEX idx_vcf_format ON vcf(format);");
        verify(stmt).execute("CREATE INDEX idx_vcf_pos ON vcf(pos);");
        verify(stmt).execute("CREATE INDEX idx_vcf_chrom_pos ON vcf(chrom, pos);");
        verify(stmt).execute("CREATE INDEX idx_contig_value ON contig(value);");
        verify(stmt).execute("CREATE INDEX idx_gtType_value ON gtType(value);");
        verify(stmt).execute("CREATE INDEX idx_info_variantId ON info(_variantId);");

        //check INDEX for nested table
        verify(stmt).execute("CREATE INDEX idx_variant_CSQ_variantId ON variant_CSQ(_variantId);");
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

    private static final String NESTED_TABLE_SQL = "CREATE TABLE variant_CSQ (_id INTEGER PRIMARY KEY AUTOINCREMENT, _variantId INTEGER REFERENCES vcf(_id), CsqIndex INTEGER, ALLELE_NUM INTEGER, SIFT TEXT, ASV_AnnotSV_ranking_criteria TEXT, PICK TEXT, SpliceAI_pred_DS_AG TEXT, CAPICE_CL TEXT, INTRON TEXT, Feature TEXT, SpliceAI_pred_DS_AL TEXT, gnomAD_AF TEXT, CLIN_SIG TEXT, Gene TEXT, HGNC_ID TEXT, FLAGS TEXT, five_prime_UTR_variant_consequence TEXT, phyloP TEXT, DISTANCE TEXT, SYMBOL_SOURCE TEXT, existing_uORFs TEXT, IncompletePenetrance TEXT, InheritanceModesGene TEXT, Consequence TEXT, MOTIF_NAME TEXT, existing_InFrame_oORFs TEXT, SOMATIC TEXT, VKGL_CL TEXT, IMPACT TEXT, MOTIF_POS TEXT, CDS_position TEXT, SYMBOL TEXT, Existing_variation TEXT, clinVar TEXT, clinVar_CLNSIG TEXT, Protein_position TEXT, SOURCE TEXT, HIGH_INF_POS TEXT, Codons TEXT, CAPICE_SC TEXT, REFSEQ_MATCH TEXT, SpliceAI_pred_DP_DG TEXT, HPO TEXT, PHENO TEXT, ASV_ACMG_class TEXT, SpliceAI_pred_DP_DL TEXT, VIPC TEXT, BIOTYPE TEXT, five_prime_UTR_variant_annotation TEXT, TRANSCRIPTION_FACTORS TEXT, clinVar_CLNSIGINCL TEXT, SpliceAI_pred_SYMBOL TEXT, ASV_AnnotSV_ranking_score TEXT, REFSEQ_OFFSET TEXT, VIPP TEXT, Grantham TEXT, Feature_type TEXT, MOTIF_SCORE_CHANGE TEXT, HGVS_OFFSET TEXT, PolyPhen TEXT, SpliceAI_pred_DP_AG TEXT, SpliceAI_pred_DS_DL TEXT, SpliceAI_pred_DS_DG TEXT, existing_OutOfFrame_oORFs TEXT, Amino_acids TEXT, HGVSp TEXT, gnomAD TEXT, STRAND TEXT, SpliceAI_pred_DP_AL TEXT, clinVar_CLNREVSTAT TEXT, gnomAD_HN TEXT, CHECK_REF TEXT, Allele TEXT, EXON TEXT, VKGL TEXT, cDNA_position TEXT, HGVSc TEXT, PUBMED TEXT) STRICT;";
    private static final String EXPECTED_INFO_TABLE = "CREATE TABLE info (_id INTEGER PRIMARY KEY AUTOINCREMENT,_variantId INTEGER REFERENCES vcf(_id),AA TEXT,NS INTEGER,AF TEXT,H2 TEXT,DP INTEGER,DB TEXT) STRICT;";
    private static final String EXPECTED_FORMAT_TABLE = "CREATE TABLE format (_id INTEGER PRIMARY KEY AUTOINCREMENT,_sampleIndex INTEGER REFERENCES sample(sampleIndex),_variantId INTEGER REFERENCES vcf(_id),HQ TEXT,GQ INTEGER,DP INTEGER,GT TEXT,_GtType INTEGER REFERENCES gtType(id)) STRICT;";
}
