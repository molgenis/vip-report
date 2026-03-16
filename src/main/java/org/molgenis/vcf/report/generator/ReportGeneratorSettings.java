package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class ReportGeneratorSettings {

  public static final int DEFAULT_MAX_NR_SAMPLES = 100;

  String appName;
  String appVersion;
  String appArguments;
  int maxNrSamples;
  Path metadataPath;
  Path sqlWasmPath;
  @Nullable Path hpoPath;
  @Nullable Path referencePath;
  @Nullable Path genesPath;
  @Nullable Path decisionTreePath;
  @Nullable Path sampleTreePath;
  @Nullable Path templateConfigPath;
}
