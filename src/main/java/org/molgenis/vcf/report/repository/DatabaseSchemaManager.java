package org.molgenis.vcf.report.repository;

import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.*;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DatabaseSchemaManager {
    public static final String TEXT_COLUMN = "%s TEXT";
    public static final String SQL_COLUMN = "%s %s";
    public static final String AUTOID_COLUMN = "id INTEGER PRIMARY KEY AUTOINCREMENT";
    private final Set<String> nestedTables = new LinkedHashSet<>();

    static final String VCF_TABLE_SQL = """
                CREATE TABLE vcf (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    chrom TEXT NOT NULL,
                    pos INTEGER NOT NULL,
                    id_vcf TEXT,
                    ref TEXT NOT NULL,
                    alt TEXT,
                    qual REAL,
                    filter TEXT
                );
            """;

    static final String HEADER_TABLE_SQL = """
                CREATE TABLE header (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    line TEXT
                );
            """;

    static final String CATEGORIES_TABLE_SQL = """
                CREATE TABLE categories (
                    id integer PRIMARY KEY AUTOINCREMENT,
                    field TEXT,
                    value TEXT,
                    label TEXT,
                    description TEXT
                );
            """;

    static final String CONFIG_TABLE_SQL = """
                CREATE TABLE config (
                    id TEXT PRIMARY KEY,
                    value TEXT
                );
            """;

    static final String PHENOTYPE_TABLE_SQL = """
                CREATE TABLE phenotype (
                    id TEXT PRIMARY KEY,
                    label TEXT
                );
            """;

    static final String SAMPLE_PHENOTYPE_TABLE_SQL = """
                CREATE TABLE samplePhenotype (
                    sample_id INTEGER PRIMARY KEY,
                    phenotype_id TEXT NOT NULL
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

    public void createTable(String sql, Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void createDatabase(ReportGeneratorSettings settings, VCFHeader vcfFileHeader, Connection connection) {
        for (String sql : generateAllTableSql(settings, vcfFileHeader)) {
            createTable(sql, connection);
        }
    }

    private List<String> generateAllTableSql(ReportGeneratorSettings reportGeneratorSettings, VCFHeader vcfFileHeader) {
        List<String> sqlStatements = new ArrayList<>();
        sqlStatements.add(VCF_TABLE_SQL);
        sqlStatements.add(CONFIG_TABLE_SQL);
        sqlStatements.add(getSampleTableSql());
        sqlStatements.add(PHENOTYPE_TABLE_SQL);
        sqlStatements.add(SAMPLE_PHENOTYPE_TABLE_SQL);
        sqlStatements.add(DECISION_TREE_TABLE_SQL);
        sqlStatements.add(getMetadataTableSql());
        sqlStatements.add(APP_METADATA_TABLE_SQL);
        sqlStatements.add(HEADER_TABLE_SQL);
        sqlStatements.add(getInfoTableSql(reportGeneratorSettings, vcfFileHeader));
        sqlStatements.add(getFormatTableSql(reportGeneratorSettings, vcfFileHeader));
        sqlStatements.add(CATEGORIES_TABLE_SQL);
        sqlStatements.addAll(nestedTables);
        return sqlStatements;
    }

    private String getSampleTableSql() {
        return String.format("""
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
                            sex IN (%s) AND
                            affectedStatus IN (%s)
                        )
                    );
                """, getSexTypes(), getAffectedStatuses());
    }

    private String getMetadataTableSql() {
        return String.format("""
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
                            fieldType IN (%s) AND
                            valueType IN (%s) AND
                            numberType IN (%s)
                       )
                   );
                """, getFieldTypes(), getValueTypes(), getNumberTypes());
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
        columns.add("variant_id INTEGER REFERENCES variant(id)");

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
        columns.add("sample_id INTEGER REFERENCES sample(id)");
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

    public String getSexTypes() {
        return Arrays.stream(Sex.values())
                .map(sex -> "'" + sex.name() + "'")
                .collect(Collectors.joining(", "));
    }

    public String getAffectedStatuses() {
        return Arrays.stream(AffectedStatus.values())
                .map(type -> "'" + type.name() + "'")
                .collect(Collectors.joining(", "));
    }

    public String getValueTypes() {
        return Arrays.stream(ValueType.values())
                .map(t -> "'" + t.name() + "'")
                .collect(Collectors.joining(", "));
    }

    public String getNumberTypes() {
        return Arrays.stream(ValueCount.Type.values())
                .map(t -> "'" + t.name() + "'")
                .collect(Collectors.joining(", "));
    }

    public String getFieldTypes() {
        return Arrays.stream(FieldType.values())
                .map(t -> "'" + t.name() + "'")
                .collect(Collectors.joining(", "));
    }

    public static String toSqlType(ValueType type, Integer count) {
        if (count != null && count != 1) {
            return "TEXT";
        }
        return switch (type) {
            case INTEGER, CATEGORICAL -> "INTEGER";
            case FLOAT -> "REAL";
            case FLAG, CHARACTER, STRING -> "TEXT";
        };
    }
}
