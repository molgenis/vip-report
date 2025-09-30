package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.utils.metadata.ValueType;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaManagerTest {
    private Connection connection;
    private DatabaseSchemaManager schemaManager;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        schemaManager = new DatabaseSchemaManager();
    }

    @Test
    void testGetSexTypesAndAffectedStatuses() {
        String result = schemaManager.getSexTypes();
        assertTrue(result.contains("'MALE'") || result.contains("'FEMALE'")); // Depending on enum

        String as = schemaManager.getAffectedStatuses();
        assertTrue(as.contains("'AFFECTED'") || as.contains("'UNAFFECTED'")); // Depending on enum
    }

    @Test
    void testToSqlTypeWithCount() {
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 2));
        assertEquals("INTEGER", DatabaseSchemaManager.toSqlType(ValueType.INTEGER, 1));
        assertEquals("TEXT", DatabaseSchemaManager.toSqlType(ValueType.STRING, 1));
    }

    @Test
    void testCreateTableExecutesSql() throws Exception {
        Statement stmt = mock(Statement.class);
        when(connection.createStatement()).thenReturn(stmt);

        schemaManager.createTable("CREATE TABLE header (id INTEGER PRIMARY KEY AUTOINCREMENT);", connection);
        verify(connection, times(1)).createStatement();
        verify(stmt, times(1)).execute("CREATE TABLE header (id INTEGER PRIMARY KEY AUTOINCREMENT);");
    }
}
