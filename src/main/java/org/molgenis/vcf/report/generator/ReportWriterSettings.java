package org.molgenis.vcf.report.generator;

import java.nio.file.Path;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
public class ReportWriterSettings {

  Path templatePath;
  boolean prettyPrint;
}
