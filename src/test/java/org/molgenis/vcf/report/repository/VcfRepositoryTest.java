package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VcfRepositoryTest {

  private Connection conn;
  private PreparedStatement stmt;
  private ResultSet resultSet;
  private VcfRepository repo;

  @BeforeEach
  void setUp() throws Exception {
    conn = mock(Connection.class);
    stmt = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    when(conn.prepareStatement(anyString(), anyInt())).thenReturn(stmt);
    when(stmt.getGeneratedKeys()).thenReturn(resultSet);
    repo = new VcfRepository();
  }

  @Test
  void testInsertVariant() throws Exception {
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(42);

    VariantContext vc = mock(VariantContext.class);
    when(vc.getContig()).thenReturn("chr1");
    when(vc.getStart()).thenReturn(12345);
    when(vc.getID()).thenReturn("rs1");
    when(vc.getReference()).thenReturn(Allele.create("A", true));
    when(vc.getAlternateAlleles()).thenReturn(List.of(Allele.create("T")));
    when(vc.hasLog10PError()).thenReturn(true);
    when(vc.getPhredScaledQual()).thenReturn(50.0);
    when(vc.filtersWereApplied()).thenReturn(true);
    when(vc.isNotFiltered()).thenReturn(true);

    Map<Object, Integer> contigIds = Map.of("chr1", 0);
    int result = repo.insertVariant(conn, vc, contigIds, 1);
    assertEquals(42, result);

    verify(stmt).setInt(1, 0);
    verify(stmt).setInt(2, 12345);
    verify(stmt).setString(3, "[\"rs1\"]");
    verify(stmt).setString(4, "A");
    verify(stmt).setString(5, "[\"T\"]");
    verify(stmt).setDouble(6, 50.0d);
    verify(stmt).setString(7, "[\"PASS\"]");
    verify(stmt, atLeastOnce()).executeUpdate();
    verify(resultSet).next();
    verify(resultSet).getInt(1);
  }
}
