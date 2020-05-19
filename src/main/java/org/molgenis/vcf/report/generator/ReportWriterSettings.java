package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
public class ReportWriterSettings {

  boolean overwriteOutputReport;
  @Nullable
  Path templatePath;
  boolean prettyPrint;
}
