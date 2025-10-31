package org.molgenis.vcf.report.repository;

import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.*;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Component
public class DatabaseSchemaManager {
    public static final String TEXT_COLUMN = "%s TEXT";
    public static final String SQL_COLUMN = "%s %s";
    public static final String AUTOID_COLUMN = "id INTEGER PRIMARY KEY AUTOINCREMENT";
    private final Set<String> nestedTables = new LinkedHashSet<>();

    static final String VCF_TABLE_SQL = """
                CREATE TABLE vcf (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  chrom INTEGER NOT NULL,
                  pos INTEGER NOT NULL,
                  id_vcf TEXT,
                  ref TEXT NOT NULL,
                  alt TEXT NOT NULL,
                  qual REAL,
                  filter INTEGER,
                  FOREIGN KEY (chrom) REFERENCES contig(id)
                );
            """;

    static final String CONTIG_TABLE_SQL = """
                CREATE TABLE contig (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    static final String HEADER_TABLE_SQL = """
                CREATE TABLE header (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    line TEXT NOT NULL
                );
            """;

    static final String CATEGORIES_TABLE_SQL = """
                CREATE TABLE categories (
                    id integer PRIMARY KEY AUTOINCREMENT,
                    field TEXT NOT NULL,
                    value TEXT NOT NULL,
                    label TEXT NOT NULL,
                    description TEXT
                );
            """;

    static final String CONFIG_TABLE_SQL = """
                CREATE TABLE config (
                    id TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
            """;

    static final String PHENOTYPE_TABLE_SQL = """
                CREATE TABLE phenotype (
                    id TEXT PRIMARY KEY,
                    label TEXT NOT NULL
                );
            """;

    static final String SAMPLE_PHENOTYPE_TABLE_SQL = """
                CREATE TABLE samplePhenotype (
                  sample_index INTEGER NOT NULL,
                  phenotype_id INTEGER NOT NULL,
                  PRIMARY KEY (sample_index),
                  FOREIGN KEY (sample_index) REFERENCES sample(sample_index),
                  FOREIGN KEY (phenotype_id) REFERENCES phenotype(id)
                );
            """;

    static final String DECISION_TREE_TABLE_SQL = """
                CREATE TABLE decisiontree (
                    id TEXT PRIMARY KEY,
                    tree TEXT NOT NULL
                );
            """;

    static final String APP_METADATA_TABLE_SQL = """
                CREATE TABLE appMetadata (
                    id TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
            """;

    static final String SAMPLE_TABLE_SQL = """
                    CREATE TABLE sample (
                      sample_index INTEGER PRIMARY KEY,
                      familyId TEXT,
                      individualId TEXT,
                      paternalId INTEGER,
                      maternalId INTEGER,
                      sex INTEGER NOT NULL,
                      affectedStatus INTEGER NOT NULL,
                      proband INTEGER,
                      FOREIGN KEY (paternalId) REFERENCES sample(sample_index),
                      FOREIGN KEY (maternalId) REFERENCES sample(sample_index)
                      FOREIGN KEY (sex) REFERENCES sex(id),
                      FOREIGN KEY (affectedStatus) REFERENCES affectedStatus(id)
                    );
                """;

    static final String AFFECTED_TABLE_SQL = """
                CREATE TABLE affectedStatus (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    static final String SEX_TABLE_SQL = """
                CREATE TABLE sex (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    static final String METADATA_TABLE_SQL = """
                    CREATE TABLE metadata (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      name TEXT,
                      fieldType INTEGER NOT NULL,
                      valueType INTEGER NOT NULL,
                      numberType INTEGER NOT NULL,
                      numberCount INTEGER,
                      required INTEGER NOT NULL,
                      separator TEXT,
                      categories TEXT,
                      label TEXT,
                      description TEXT,
                      parent TEXT,
                      nested INTEGER,
                      nullValue TEXT,
                      FOREIGN KEY (fieldType) REFERENCES fieldType(id),
                      FOREIGN KEY (valueType) REFERENCES valueType(id),
                      FOREIGN KEY (numberType) REFERENCES numberType(id)
                    );
                """;

    static final String FIELDTYPE_TABLE_SQL = """
                CREATE TABLE fieldType (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    static final String VALUETYPE_TABLE_SQL = """
                CREATE TABLE valueType (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    static final String NUMBERTYPE_TABLE_SQL = """
                CREATE TABLE numberType (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                );
            """;

    public void executeSql(String sql, Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void createDatabase(ReportGeneratorSettings settings, VCFHeader vcfFileHeader, Connection connection) {
        executeSql("PRAGMA foreign_keys = ON;", connection);
        for (String sql : generateAllTableSql(settings, vcfFileHeader)) {
            executeSql(sql, connection);
        }
    }

    private List<String> generateAllTableSql(ReportGeneratorSettings reportGeneratorSettings, VCFHeader vcfFileHeader) {
        List<String> sqlStatements = new ArrayList<>();
        sqlStatements.add(CONTIG_TABLE_SQL);
        sqlStatements.add(VCF_TABLE_SQL);
        sqlStatements.add(CONFIG_TABLE_SQL);
        sqlStatements.add(AFFECTED_TABLE_SQL);
        sqlStatements.add(SAMPLE_TABLE_SQL);
        sqlStatements.add(SEX_TABLE_SQL);
        sqlStatements.add(PHENOTYPE_TABLE_SQL);
        sqlStatements.add(SAMPLE_PHENOTYPE_TABLE_SQL);
        sqlStatements.add(DECISION_TREE_TABLE_SQL);
        sqlStatements.add(FIELDTYPE_TABLE_SQL);
        sqlStatements.add(NUMBERTYPE_TABLE_SQL);
        sqlStatements.add(VALUETYPE_TABLE_SQL);
        sqlStatements.add(METADATA_TABLE_SQL);
        sqlStatements.add(APP_METADATA_TABLE_SQL);
        sqlStatements.add(HEADER_TABLE_SQL);
        sqlStatements.add(getInfoTableSql(reportGeneratorSettings, vcfFileHeader));
        sqlStatements.add(getFormatTableSql(reportGeneratorSettings, vcfFileHeader));
        sqlStatements.add(CATEGORIES_TABLE_SQL);
        sqlStatements.addAll(nestedTables);
        return sqlStatements;
    }

    public String getInfoTableSql(ReportGeneratorSettings settings, VCFHeader vcfFileHeader) {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService, vcfFileHeader);
        return buildInfoTable(fieldMetadatas.getInfo());
    }

    public String getFormatTableSql(ReportGeneratorSettings settings, VCFHeader vcfFileHeader) {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService, vcfFileHeader);
        return buildFormatTable(fieldMetadatas.getFormat());
    }

    private FieldMetadatas loadFieldMetadatas(FieldMetadataService service, VCFHeader vcfFileHeader) {
        return service.load(vcfFileHeader);
    }

    private String buildInfoTable(Map<String, FieldMetadata> infoFields) {
        StringBuilder infoBuilder = new StringBuilder("CREATE TABLE info (");
        List<String> columns = new ArrayList<>();
        columns.add(AUTOID_COLUMN);
        columns.add("variant_id INTEGER REFERENCES vcf(id)");

        for (var entry : infoFields.entrySet()) {
            FieldMetadata meta = entry.getValue();
            if (meta.getNestedFields() == null || meta.getNestedFields().isEmpty()) {
                if (meta.getNumberType() == ValueCount.Type.FIXED && meta.getNumberCount() == 1) {
                    columns.add(String.format(SQL_COLUMN, entry.getKey(), toSqlType(meta.getType(), meta.getNumberCount())));
                } else {
                    columns.add(String.format(TEXT_COLUMN, entry.getKey()));
                }
            } else {
                String nestedTableSql = buildNestedTable("variant", entry.getKey(), meta.getNestedFields());
                nestedTables.add(nestedTableSql);
            }
        }
        infoBuilder.append(String.join(",", columns));
        infoBuilder.append(");");
        return infoBuilder.toString();
    }

    private String buildFormatTable(Map<String, FieldMetadata> formatFields) {
        StringBuilder formatBuilder = new StringBuilder("CREATE TABLE format (");
        List<String> columns = new ArrayList<>();
        columns.add(AUTOID_COLUMN);
        columns.add("sample_index INTEGER REFERENCES sample(sample_index)");
        columns.add("variant_id INTEGER REFERENCES vcf(id)");

        for (var entry : formatFields.entrySet()) {
            FieldMetadata meta = entry.getValue();
            if (meta.getNestedFields() == null || meta.getNestedFields().isEmpty()) {
                if (meta.getNumberType() == ValueCount.Type.FIXED && meta.getNumberCount() == 1) {
                    columns.add(String.format(SQL_COLUMN, entry.getKey(), toSqlType(meta.getType(), meta.getNumberCount())));
                    if(entry.getKey().equals("GT")){
                        columns.add(String.format(SQL_COLUMN, "GT_type", "TEXT"));
                    }
                } else {
                    columns.add(String.format(TEXT_COLUMN, entry.getKey()));
                }
            } else {
                throw new UnsupportedOperationException("Nested Formats are not yet supported");
            }
        }
        formatBuilder.append(String.join(",", columns));
        formatBuilder.append(");");
        return formatBuilder.toString();
    }

    private String buildNestedTable(String prefix, String postfix, Map<String, NestedFieldMetadata> nestedFieldMap) {
        String tableName = String.format("%s_%s", prefix, postfix);
        StringBuilder nestedBuilder = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        List<String> nestedColumns = new ArrayList<>();
        nestedColumns.add(AUTOID_COLUMN);
        if (tableName.startsWith("variant_")) {
            nestedColumns.add("variant_id INTEGER REFERENCES vcf(id)");
        } else if (tableName.startsWith("format_")) {
            nestedColumns.add("format_id INTEGER REFERENCES format(id)");
        }
        for (var nestedEntry : nestedFieldMap.entrySet()) {
            NestedFieldMetadata nestedField = nestedEntry.getValue();
            String columnName = nestedEntry.getKey();
            if (nestedField.getNumberType() == ValueCount.Type.FIXED && nestedField.getNumberCount() == 1) {
                nestedColumns.add(String.format(SQL_COLUMN, columnName, toSqlType(nestedField.getType(), nestedField.getNumberCount())));
            } else {
                nestedColumns.add(String.format(TEXT_COLUMN, columnName));
            }
        }
        nestedBuilder.append(String.join(", ", nestedColumns));
        nestedBuilder.append(");");
        return nestedBuilder.toString();
    }

    public static String toSqlType(ValueType type, Integer count) {
        if (count != null && count != 1) {
            return "TEXT";
        }
        return switch (type) {
            case FLAG, INTEGER, CATEGORICAL -> "INTEGER";
            case FLOAT -> "REAL";
            case CHARACTER, STRING -> "TEXT";
        };
    }
}
