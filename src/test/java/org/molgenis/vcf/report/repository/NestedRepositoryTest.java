package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;

class NestedRepositoryTest {

  private NestedRepository nestedRepository;

  @BeforeEach
  void setUp() {
    nestedRepository = new NestedRepository();
  }

  @Test
  void testInsertNested() throws SQLException {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(conn.createStatement()).thenReturn(ps);
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("field")).thenReturn("INFO/CSQ/Consequence")
        .thenReturn("INFO/CSQ/Consequence");
    when(rs.getString("value")).thenReturn("synonymous_variant").thenReturn("missense_variant");
    when(rs.getInt("id")).thenReturn(1).thenReturn(2);
    when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(ps.executeQuery("SELECT id, field, value FROM categories")).thenReturn(rs);

    String fieldName = "CSQ";
    List<String> matchingNestedFields = new ArrayList<>();
    matchingNestedFields.addAll(List.of("Gene", "Consequence"));
    VariantContext vc = mock(VariantContext.class);
    when(vc.hasAttribute(fieldName)).thenReturn(true);
    List<String> nestedEntries = Arrays.asList("GENE1|missense_variant",
        "GENE2|synonymous_variant");
    when(vc.getAttributeAsStringList(eq(fieldName), anyString())).thenReturn(nestedEntries);

    FieldMetadatas fieldMetadatas = mock(FieldMetadatas.class);
    FieldMetadata parentMeta = mock(FieldMetadata.class);
    Map<String, FieldMetadata> infoMap = new HashMap<>();
    infoMap.put(fieldName, parentMeta);
    when(fieldMetadatas.getInfo()).thenReturn(infoMap);

    when(parentMeta.getSeparator()).thenReturn('|');

    NestedFieldMetadata geneMeta = mock(NestedFieldMetadata.class);
    when(geneMeta.getIndex()).thenReturn(0);
    when(geneMeta.getType()).thenReturn(null);
    NestedFieldMetadata consMeta = mock(NestedFieldMetadata.class);
    when(consMeta.getIndex()).thenReturn(1);
    when(consMeta.getType()).thenReturn(org.molgenis.vcf.utils.metadata.ValueType.CATEGORICAL);

    Map<String, NestedFieldMetadata> nestedFieldsMap = new HashMap<>();
    nestedFieldsMap.put("Gene", geneMeta);
    nestedFieldsMap.put("Consequence", consMeta);
    when(parentMeta.getNestedFields()).thenReturn(nestedFieldsMap);

    nestedRepository.insertNested(conn, fieldName, vc, matchingNestedFields, fieldMetadatas, 1,
        true);

    verify(conn).prepareStatement(anyString());
    verify(ps).setInt(1, 1);
    verify(ps).setString(2, "[\"GENE1\"]");
    verify(ps).setString(3, "[1]");
    verify(ps).setString(2, "[\"GENE2\"]");
    verify(ps).setString(3, "[2]");

    verify(ps, times(2)).addBatch();
    verify(ps).executeBatch();
  }
}
