package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import java.util.List;
import lombok.Value;

@Value
public class SampleSettings {
  List<Path> pedigreePaths;
  String phenotypeString;
}
