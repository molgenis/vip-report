package org.molgenis.vcf.report.utils;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.repository.FieldValueKey;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

class CategoryUtilsTest {

    @Test
    void testLoadCategoriesMap() throws SQLException {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("field")).thenReturn("Gene", "Effect");
        when(rs.getString("value")).thenReturn("COL7A1", "missense_variant");
        when(rs.getInt("id")).thenReturn(7, 12);

        Map<FieldValueKey, Integer> result = CategoryUtils.loadCategoriesMap(conn);

        assertEquals(7, result.get(new FieldValueKey("Gene", "COL7A1")));
        assertEquals(12, result.get(new FieldValueKey("Effect", "missense_variant")));
    }

    @Test
    void testAddCategorical_single() throws SQLException {
        FieldMetadata meta = mock(FieldMetadata.class);
        when(meta.getNumberCount()).thenReturn(1);

        Map<FieldValueKey, Integer> lookup = new HashMap<>();
        lookup.put(new FieldValueKey("INFO/Gene", "TEST1"), 5);

        PreparedStatement ps = mock(PreparedStatement.class);

        CategoryUtils.addCategorical(INFO, meta, lookup, "Gene", "TEST1", ps, 2);

        verify(ps).setInt(2, 5);
    }

    @Test
    void testAddCategorical_multiple() throws SQLException {
        FieldMetadata meta = mock(FieldMetadata.class);
        when(meta.getNumberCount()).thenReturn(2);
        when(meta.getSeparator()).thenReturn(',');

        Map<FieldValueKey, Integer> lookup = new HashMap<>();
        lookup.put(new FieldValueKey("INFO/Gene", "TEST1"), 1);
        lookup.put(new FieldValueKey("INFO/Gene", "TEST2"), 2);

        PreparedStatement ps = mock(PreparedStatement.class);

        CategoryUtils.addCategorical(INFO, meta, lookup, "Gene", "TEST1,TEST2", ps, 3);

        verify(ps).setString(3, "[1,2]");
    }

    @Test
    void testAddCategorical_null() throws SQLException {
        FieldMetadata meta = mock(FieldMetadata.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        CategoryUtils.addCategorical(INFO, meta, Collections.emptyMap(), "Gene", null, ps, 4);

        verify(ps).setString(4, null);
    }
}
