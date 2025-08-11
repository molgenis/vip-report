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

public class DatabaseSchemaManager {

    private final ReportGeneratorSettings settings;

    // Keep state of nested table creations
    private final Set<String> nestedTables = new LinkedHashSet<>();
    private final DatabaseManager databaseManager;
    private VCFHeader vcfFileHeader;

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

        sqlStatements.add(getInfoTableSql());
        sqlStatements.add(getFormatTableSql());

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

    private String getReportDataTableSql() {
        return """
            CREATE TABLE reportdata (
                key TEXT PRIMARY KEY,
                value TEXT
            );
            """;
    }

    private String getSampleTableSql() {
        return String.format("""
            CREATE TABLE sample (
                id TEXT PRIMARY KEY,
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
                sample_id TEXT PRIMARY KEY,
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
    public String getInfoTableSql() throws IOException {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService);

        return buildInfoTable(fieldMetadatas.getInfo());
    }

    /**
     * Generates format table SQL plus nested format_* tables
     * @return SQL string for 'format' table.
     */
    public String getFormatTableSql() throws IOException {
        FieldMetadataService fieldMetadataService = new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
        FieldMetadatas fieldMetadatas = loadFieldMetadatas(fieldMetadataService);

        return buildFormatTable(fieldMetadatas.getFormat());
    }

    // Helpers for loading metadata (you can refactor this to accept param too)
    private FieldMetadatas loadFieldMetadatas(FieldMetadataService service) throws IOException {
        // Note: You may want to make the Map param to the load method configurable outside if needed
        return service.load(
                vcfFileHeader,
                Map.of(
                        FieldIdentifier.builder()
                                .type(FieldType.INFO)
                                .name("CSQ")
                                .build(),
                        NestedAttributes.builder()
                                .prefix("INFO_DESCRIPTION_PREFIX")  // use real prefix constant
                                .seperator("|")
                                .build()
                ));
    }

    private String buildInfoTable(Map<String, FieldMetadata> infoFields) {
        StringBuilder infoBuilder = new StringBuilder("CREATE TABLE info (");
        List<String> columns = new ArrayList<>();
        columns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
        columns.add("variant_id INTEGER REFERENCES variant(id)");

        for (var entry : infoFields.entrySet()) {
            FieldMetadata meta = entry.getValue();
            if (meta.getNestedFields() == null || meta.getNestedFields().isEmpty()) {
                if (meta.getNumberType() == ValueCount.Type.FIXED && meta.getNumberCount() == 1) {
                    columns.add(String.format("%s %s", entry.getKey(), toSqlType(meta.getType())));
                } else {
                    columns.add(String.format("%s TEXT", entry.getKey()));
                }
            } else {
                // Build nested table SQL here; e.g. variant_<field>
                String nestedTableSql = buildNestedTable("variant_" + entry.getKey(), meta.getNestedFields());
                nestedTables.add(nestedTableSql);

                // Normally no direct column added to this table for nested fields,
                // as they go to separate tables.
            }
        }

        infoBuilder.append(String.join(",", columns));
        infoBuilder.append(");");

        return infoBuilder.toString();
    }

    private String buildFormatTable(Map<String, FieldMetadata> formatFields) {
        StringBuilder formatBuilder = new StringBuilder("CREATE TABLE format (");
        List<String> columns = new ArrayList<>();
        columns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
        columns.add("sample_id INTEGER REFERENCES sample(id)");
        columns.add("variant_id INTEGER REFERENCES variant(id)");

        for (var entry : formatFields.entrySet()) {
            FieldMetadata meta = entry.getValue();
            if (meta.getNestedFields() == null || meta.getNestedFields().isEmpty()) {
                if (meta.getNumberType() == ValueCount.Type.FIXED && meta.getNumberCount() == 1) {
                    columns.add(String.format("%s %s", entry.getKey(), toSqlType(meta.getType())));
                } else {
                    columns.add(String.format("%s TEXT", entry.getKey()));
                }
            } else {
                // Build nested table SQL here; e.g. format_<field>
                String nestedTableSql = buildNestedTable("format_" + entry.getKey(), meta.getNestedFields());
                nestedTables.add(nestedTableSql);

                // No direct column added here for nested fields
            }
        }

        formatBuilder.append(String.join(",", columns));
        formatBuilder.append(");");

        return formatBuilder.toString();
    }

    /**
     * Builds nested table SQL strings from nested fields
     *
     * @param tableName       Name of nested table, e.g. "variant_X" or "format_X"
     * @param nestedFieldMap  Nested fields map from metadata
     * @return SQL CREATE TABLE string
     */
    private String buildNestedTable(String tableName, Map<String, NestedFieldMetadata> nestedFieldMap) {
        StringBuilder nestedBuilder = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        List<String> nestedColumns = new ArrayList<>();

        nestedColumns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
        // Determine foreign key column based on prefix
        if (tableName.startsWith("variant_")) {
            nestedColumns.add("variant_id INTEGER REFERENCES variant(id)");
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
                nestedColumns.add(String.format("%s %s", columnName, toSqlType(nestedField.getType())));
            } else {
                nestedColumns.add(String.format("%s TEXT", columnName));
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

    private String toSqlType(ValueType type) {
        switch (type) {
            case INTEGER:
                return "INTEGER";
            case FLOAT:
                return "REAL";
            case FLAG:
            case CHARACTER:
            case STRING:
            case CATEGORICAL:
                return "TEXT";
            default:
                throw new IllegalArgumentException("Unexpected ValueType: " + type);
        }
    }
}
