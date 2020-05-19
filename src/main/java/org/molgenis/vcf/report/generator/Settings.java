package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.NonNull;
import lombok.Value;

@Value
public class Settings {

  @NonNull Path inputVcfPath;
  @NonNull ReportGeneratorSettings reportGeneratorSettings;
  @NonNull Path outputReportPath;
  boolean overwriteOutputReport;
  @NonNull ReportWriterSettings reportWriterSettings;
}
