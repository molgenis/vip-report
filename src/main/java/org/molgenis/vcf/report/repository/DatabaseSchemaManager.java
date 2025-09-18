package org.molgenis.vcf.report.repository;

import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.*;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Sex;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.generator.ReportGenerator.INFO_DESCRIPTION_PREFIX;

public class DatabaseSchemaManager {

    public static final String TEXT_COLUMN = "%s TEXT";
    public static final String SQL_COLUMN = "%s %s";
    public static final String INTEGER_COLUMN = "%s INTEGER";
    public static final String AUTOID_COLUMN = "id INTEGER PRIMARY KEY AUTOINCREMENT";
    private final ReportGeneratorSettings settings;

    // Keep state of nested table creations
    private final Set<String> nestedTables = new LinkedHashSet<>();
    private final DatabaseManager databaseManager;
    private final VCFHeader vcfFileHeader;

    public DatabaseSchemaManager(ReportGeneratorSettings settings, VCFHeader vcfFileHeader, DatabaseManager databaseManager) {
        this.settings = requireNonNull(settings);
        this.vcfFileHeader = requireNonNull(vcfFileHeader);
        this.databaseManager = requireNonNull(databaseManager);
    }

    public void createDatabase(){
        try {
            for(String sql : generateAllTableSql()) {
                databaseManager.createTable(sql);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> generateAllTableSql() throws IOException {
        List<String> sqlStatements = new ArrayList<>();

        sqlStatements.add(getVcfTableSql());
        sqlStatements.add(getReportDataTableSql());
        sqlStatements.add(getSampleTableSql());
        sqlStatements.add(getPhenotypeTableSql());
        sqlStatements.add(getSamplePhenotypeTableSql());
        sqlStatements.add(getDecisionTreeTableSql());
        sqlStatements.add(getMetadataTableSql());
        sqlStatements.add(getReportMetadataTableSql());
        sqlStatements.add(getHeaderSql());
        sqlStatements.add(getInfoTableSql());
        sqlStatements.add(getFormatTableSql());
        sqlStatements.add(getCategoricalTableSql());

        sqlStatements.addAll(nestedTables); // nested tables accumulated during info/format generation

        return sqlStatements;
    }

    private String getVcfTableSql() {
        return """
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
    }

    private String getHeaderSql() {
        return """
            CREATE TABLE header (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                line TEXT
            );
            """;
    }

    private String getCategoricalTableSql() {
        return """
            CREATE TABLE categories (
                id integer PRIMARY KEY AUTOINCREMENT,
                field TEXT,
                value TEXT,
                label TEXT,
                description TEXT
            );
            """;
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

    private String getReportDataTableSql() {
        return """
            CREATE TABLE reportdata (
                id TEXT PRIMARY KEY,
                value TEXT
            );
            """;
    }

    private String getPhenotypeTableSql() {
        return """
            CREATE TABLE phenotype (
                id TEXT PRIMARY KEY,
                label TEXT
            );
            """;
    }

    private String getSamplePhenotypeTableSql() {
        return """
            CREATE TABLE samplePhenotype (
                sample_id INTEGER PRIMARY KEY,
                phenotype_id TEXT NOT NULL
            );
            """;
    }

    private String getDecisionTreeTableSql() {
        return """
            CREATE TABLE decisiontree (
                id TEXT PRIMARY KEY,
                tree TEXT NOT NULL
            );
            """;
    }

    private String getReportMetadataTableSql() {
        return """
            CREATE TABLE reportMetadata (
                id TEXT PRIMARY KEY,
                value TEXT NOT NULL
            );
            """;
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

    /**
     * Generates info table SQL plus nested variant_* tables.
     * @return SQL string for 'info' table.
     */
    public String getInfoTableSql() {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService);

        return buildInfoTable(fieldMetadatas.getInfo());
    }

    /**
     * Generates format table SQL plus nested format_* tables
     * @return SQL string for 'format' table.
     */
    public String getFormatTableSql() {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService);

        return buildFormatTable(fieldMetadatas.getFormat());
    }

    private FieldMetadatas loadFieldMetadatas(FieldMetadataService service) {
        return service.load(
                vcfFileHeader,
                Map.of(FieldIdentifier.builder().type(FieldType.INFO).name("CSQ").build(), NestedAttributes.builder().prefix(INFO_DESCRIPTION_PREFIX).seperator("|").build()));
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
        // Determine foreign key column based on prefix
        if (tableName.startsWith("variant_")) {
            nestedColumns.add("variant_id INTEGER REFERENCES vcf(id)");
        } else if (tableName.startsWith("format_")) {
            nestedColumns.add("format_id INTEGER REFERENCES format(id)");
        } else {
            // add fallback or other keys if needed
        }

        for (var nestedEntry : nestedFieldMap.entrySet()) {
            NestedFieldMetadata nestedField = nestedEntry.getValue();
            String columnName = nestedEntry.getKey();

            //FIXME: bug in utils?
            if(columnName.equals(" from Ensembl VEP. Format: Allele")){
                columnName = "Allele";
            }

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

    // Enum utilities methods left basically the same, but public or private as needed...

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
        if(count != null &&  count != 1){
            return "TEXT";
        }
        return switch (type) {
            case INTEGER, CATEGORICAL -> "INTEGER";
            case FLOAT -> "REAL";
            case FLAG, CHARACTER, STRING -> "TEXT";
        };
    }
}
