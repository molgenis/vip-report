package org.molgenis.vcf.report.generator;

import java.nio.file.Path;

public interface ReportIdGenerator {
  String generate(Path htsFile);
}
