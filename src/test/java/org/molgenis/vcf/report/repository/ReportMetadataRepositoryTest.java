package org.molgenis.vcf.report.repository;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.model.metadata.HtsFile;
import org.molgenis.vcf.utils.model.metadata.HtsFormat;

@ExtendWith(MockitoExtension.class)
class ReportMetadataRepositoryTest {

  private Connection conn;
  private PreparedStatement stmt;
  private ReportMetadataRepository repo;

  @BeforeEach
  void setUp() throws Exception {
    conn = mock(Connection.class);
    stmt = mock(PreparedStatement.class);
    when(conn.prepareStatement(anyString())).thenReturn(stmt);
    repo = new ReportMetadataRepository();
  }

  @Test
  void testInsertReportMetadataBatching() throws SQLException {
    ReportMetadata reportMetadata = mock(ReportMetadata.class);
    var appMeta = mock(org.molgenis.vcf.report.model.metadata.AppMetadata.class);
    when(reportMetadata.getAppMetadata()).thenReturn(appMeta);
    when(appMeta.getAppArguments()).thenReturn("ARGS");
    when(appMeta.getAppName()).thenReturn("NAME");
    when(appMeta.getAppVersion()).thenReturn("VERSION");

    HtsFile htsFileObj = mock(HtsFile.class);
    when(htsFileObj.getGenomeAssembly()).thenReturn("assembly");
    when(htsFileObj.getUri()).thenReturn("uri");
    when(htsFileObj.getHtsFormat()).thenReturn(HtsFormat.VCF);
    when(reportMetadata.getHtsFile()).thenReturn(htsFileObj);

    doNothing().when(stmt).addBatch();
    doReturn(new int[] {1}).when(stmt).executeBatch();

    repo.insertReportMetadata(conn, reportMetadata);

    verify(stmt).setString(1, "appArguments");
    verify(stmt).setString(2, "ARGS");
    verify(stmt).setString(1, "version");
    verify(stmt).setString(2, "VERSION");
    verify(stmt).setString(1, "name");
    verify(stmt).setString(2, "NAME");
    verify(stmt).setString(1, "htsFile");
    verify(stmt)
        .setString(2, "{\"uri\":\"uri\",\"htsFormat\":\"VCF\",\"genomeAssembly\":\"assembly\"}");
    verify(stmt, times(4)).addBatch();
    verify(stmt, times(1)).executeBatch();
  }
}
