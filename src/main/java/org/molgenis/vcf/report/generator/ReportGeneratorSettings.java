package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.NonNull;
import lombok.Value;

@Value
public class ReportGeneratorSettings {

  public static final int DEFAULT_MAX_NR_SAMPLES = 100;
  public static final int DEFAULT_MAX_NR_RECORDS = 100;

  @NonNull String appName;
  @NonNull String appVersion;
  @NonNull String appArguments;
  int maxNrSamples;
  int maxNrRecords;
  Path referencePath;
}
