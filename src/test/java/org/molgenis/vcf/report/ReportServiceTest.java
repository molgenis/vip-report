package org.molgenis.vcf.report;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.ReportService;
import org.molgenis.vcf.report.generator.ReportWriter;
import org.molgenis.vcf.report.generator.ReportWriterSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.generator.Settings;
import org.molgenis.vcf.report.model.Binary;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Report;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @TempDir static Path sharedTempDir;

  @Mock private ReportGenerator reportGenerator;
  @Mock private ReportWriter reportWriter;
  private ReportService reportService;

  @BeforeEach
  void setUpBeforeEach() {
    reportService = new ReportService(reportGenerator, reportWriter);
  }

  @Test
  void createReport() {
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArguments = "MyArguments";
    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    Path outputReportPath = sharedTempDir.resolve("example.vcf.html");
    Path metadataPath = sharedTempDir.resolve("field_metadata.json");

    Report report =
        new Report(
            new Binary( null, null, Map.of()),
            new Bytes("FIXME".getBytes())
        );

    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName,
            appVersion,
            appArguments,
            ReportGeneratorSettings.DEFAULT_MAX_NR_SAMPLES,
            metadataPath,
            null,
            null,
            null,null,
            null);
    ReportWriterSettings reportWriterSettings = new ReportWriterSettings(null, true);
    SampleSettings sampleSettings = new SampleSettings(null, null, null, Map.of());
    Settings settings =
        new Settings(
            inputVcfPath,
            reportGeneratorSettings,
            outputReportPath,
            true,
            reportWriterSettings,
            sampleSettings);
    when(reportGenerator.generateReport(inputVcfPath, sampleSettings, reportGeneratorSettings))
        .thenReturn(report);

    reportService.createReport(settings);

    verify(reportWriter).write(report, outputReportPath, reportWriterSettings);
  }
}
