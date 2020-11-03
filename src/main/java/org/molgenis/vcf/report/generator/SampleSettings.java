package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import java.util.List;
import lombok.Value;

@Value
public class SampleSettings {
  List<String> probandNames;
  List<Path> pedigreePaths;
  String phenotypeString;
}
