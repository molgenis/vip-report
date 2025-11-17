package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

public class MissingSampleException extends RuntimeException {

  public MissingSampleException(String individualId) {
    super(format("No sample found for sampleId '%s'", individualId));
  }
}
