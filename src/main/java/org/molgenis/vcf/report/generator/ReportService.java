package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.model.Report;
import org.springframework.stereotype.Component;

@Component
public class ReportService {

  private final ReportGenerator reportGenerator;
  private final ReportWriter reportWriter;

  public ReportService(ReportGenerator reportGenerator, ReportWriter reportWriter) {
    this.reportGenerator = requireNonNull(reportGenerator);
    this.reportWriter = requireNonNull(reportWriter);
  }

  public void createReport(Settings settings) {
    Path inputVcfPath = settings.getInputVcfPath();
    SampleSettings sampleSettings = settings.getSampleSettings();
    Report report =
        reportGenerator.generateReport(
            inputVcfPath,
            sampleSettings.getPedigreePaths(),
            sampleSettings.getPhenotypeString(),
            settings.getReportGeneratorSettings());

    Path outputReportPath = settings.getOutputReportPath();
    reportWriter.write(report, outputReportPath, settings.getReportWriterSettings());
  }
}
