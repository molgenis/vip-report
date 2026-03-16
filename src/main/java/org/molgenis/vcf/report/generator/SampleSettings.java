package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class SampleSettings {
  List<String> probandNames;
  @Nullable List<Path> pedigreePaths;
  @Nullable String phenotypeString;
  Map<String, CramPath> cramPaths;

  @Value
  public static class CramPath {
    Path cram;
    Path crai;
  }
}
