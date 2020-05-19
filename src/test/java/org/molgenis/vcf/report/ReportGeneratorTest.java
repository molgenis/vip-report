package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.ReportMetadata;
import org.molgenis.vcf.report.model.Sample;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

  @Mock private HtsJdkMapper htsJdkMapper;
  private ReportGenerator reportGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    reportGenerator = new ReportGenerator(htsJdkMapper);
  }

  @Test
  void generateReport() {
    int maxNrSamples = 10;
    int maxNrRecords = 100;

    Items<Sample> sampleItems = new Items<>(emptyList(), 3);
    when(htsJdkMapper.mapSamples(any(VCFHeader.class), eq(maxNrSamples))).thenReturn(sampleItems);

    Items<Record> recordItems = new Items<>(emptyList(), 5);
    when(htsJdkMapper.mapRecords(any(), eq(maxNrRecords), eq(emptyList()))).thenReturn(recordItems);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArgs = "MyArgs";
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(appName, appVersion, appArgs, maxNrSamples, maxNrRecords);
    Report report =
        new Report(
            new ReportMetadata(appName, appVersion, appArgs),
            new ReportData(sampleItems, recordItems));
    assertEquals(report, reportGenerator.generateReport(inputVcfPath, reportGeneratorSettings));
  }
}
