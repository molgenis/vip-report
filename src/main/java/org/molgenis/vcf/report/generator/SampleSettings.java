package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class SampleSettings {
  List<String> probandNames;
  List<Path> pedigreePaths;
  String phenotypeString;
  Map<String, CramPath> cramPaths;
  Map<String, RnaPath> rnaPaths;
  @Value
  public static class CramPath {
    Path cram;
    Path crai;
  }
  @Value
  public static class RnaPath {
    Path bw;
    Path bed;
  }
}
