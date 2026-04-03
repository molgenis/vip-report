package org.molgenis.vcf.report.generator;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DefaultReportIdGenerator implements ReportIdGenerator {
  @Override
  public String generate() {
    return UUID.randomUUID().toString();
  }
}