package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Settings {

  @NonNull Path inputVcfPath;
  @NonNull ReportGeneratorSettings reportGeneratorSettings;
  @NonNull Path outputReportPath;
  boolean overwriteOutputReport;
  @NonNull ReportWriterSettings reportWriterSettings;
  @NonNull SampleSettings sampleSettings;
}
