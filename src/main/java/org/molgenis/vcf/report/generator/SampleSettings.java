package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.Value;

@Value
public class SampleSettings {
  Path pedigreePath;
  String phenotypeString;
}
