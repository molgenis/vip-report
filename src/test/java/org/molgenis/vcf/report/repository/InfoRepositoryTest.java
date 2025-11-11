package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.utils.metadata.FieldType;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.molgenis.vcf.utils.metadata.FieldType.INFO;

class InfoRepositoryTest {

    private InfoRepository infoRepository;

    @BeforeEach
    void setUp() {
        infoRepository = new InfoRepository();
    }

    @Test
    void testInsertInfoData() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(conn.createStatement()).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("field")).thenReturn("INFO/CAT");
        when(rs.getString("value")).thenReturn("A");
        when(rs.getInt("id")).thenReturn(1);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(ps.executeQuery("SELECT id, field, value FROM categories")).thenReturn(rs);

        List<String> infoColumns = Arrays.asList("FLAG", "CAT", "INT_ARRAY", "SIMPLE");
        FieldMetadatas fieldMetadatas = mock(FieldMetadatas.class);
        Map<String, FieldMetadata> infoMetaMap = new HashMap<>();
        FieldMetadata flagMeta = mock(FieldMetadata.class);
        FieldMetadata catMeta = mock(FieldMetadata.class);
        FieldMetadata arrMeta = mock(FieldMetadata.class);
        FieldMetadata simpleMeta = mock(FieldMetadata.class);

        when(flagMeta.getType()).thenReturn(org.molgenis.vcf.utils.metadata.ValueType.FLAG);
        when(catMeta.getType()).thenReturn(org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL);

        when(arrMeta.getNumberType()).thenReturn(org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED);
        when(arrMeta.getNumberCount()).thenReturn(2);
        when(arrMeta.getSeparator()).thenReturn(',');

        infoMetaMap.put("FLAG", flagMeta);
        infoMetaMap.put("CAT", catMeta);
        infoMetaMap.put("INT_ARRAY", arrMeta);
        infoMetaMap.put("SIMPLE", simpleMeta);
        when(fieldMetadatas.getInfo()).thenReturn(infoMetaMap);

        VariantContext vc = mock(VariantContext.class);
        when(vc.getAttribute(eq("FLAG"), isNull())).thenReturn(null); // No value for FLAG field
        Object catVal = "A";
        when(vc.getAttribute(eq("CAT"), isNull())).thenReturn(catVal);
        when(vc.getAttribute(eq("INT_ARRAY"), isNull())).thenReturn("1,2");
        when(vc.getAttribute(eq("SIMPLE"), isNull())).thenReturn("B");

        infoRepository.insertInfoData(conn, vc, infoColumns, fieldMetadatas, 1, true);

        verify(conn).prepareStatement(anyString());
        verify(ps).setInt(1, 1);
        verify(ps).setInt(2, 0);
        verify(ps).setString(3, "[1]");
        verify(ps).setString(4, "[\"1\",\"2\"]");
        verify(ps).setString(5, "[\"B\"]");
        verify(ps).executeUpdate();
    }

    @Test
    void testInsertInfoOrderData() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(conn.createStatement()).thenReturn(ps);

        Map<FieldType, Map<String, Integer>> metadataKeys = new HashMap<>();
        Map<String, Integer> infoKeys = Map.of("TEST", 1,"TEST2", 2,"TEST3", 3);
        metadataKeys.put(INFO, infoKeys);
        int variantId = 1;
        String[] infoItems = "TEST3,TEST,TEST2".split(",");
        infoRepository.insertInfoFieldOrder(conn, metadataKeys,infoItems, variantId );

        verify(conn).prepareStatement(anyString());
        verify(ps).setInt(1, 0);
        verify(ps, times(3)).setInt(2, 1);
        verify(ps).setInt(3, 1);
        verify(ps).setInt(1, 1);
        verify(ps).setInt(3, 2);
        verify(ps).setInt(1, 0);
        verify(ps).setInt(3, 1);
        verify(ps, times(3)).addBatch();
        verify(ps).executeBatch();
    }
}
