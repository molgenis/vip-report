package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Settings {
  Path inputVcfPath;
  ReportGeneratorSettings reportGeneratorSettings;
  Path outputReportPath;
  boolean overwriteOutputReport;
  ReportWriterSettings reportWriterSettings;
  SampleSettings sampleSettings;
}
