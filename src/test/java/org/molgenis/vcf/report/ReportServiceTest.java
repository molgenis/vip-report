package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.utils.model.metadata.HtsFormat.VCF;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
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
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.model.metadata.HtsFile;

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
  void createReport() throws IOException {
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArguments = "MyArguments";
    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    Path outputReportPath = sharedTempDir.resolve("example.vcf.html");

    Report report =
        new Report(
            new ReportMetadata(
                new AppMetadata(appName, appVersion, appArguments),
                new HtsFile(inputVcfPath.toString(), VCF, "UNKNOWN")),
            new ReportData(emptyList(), emptyList()),
            new Binary(new Bytes(Files.readAllBytes(inputVcfPath)), null, null, Map.of()),
            new ObjectMapper()
                .readValue(
                    "{\"name\":\"testtree\", \"description\":\"no need for a valid tree\"}",
                    Map.class), null);
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName,
            appVersion,
            appArguments,
            ReportGeneratorSettings.DEFAULT_MAX_NR_SAMPLES,
            null,
            null,
            null,
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
