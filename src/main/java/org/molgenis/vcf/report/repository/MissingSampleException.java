package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import java.io.Serial;

public class MissingSampleException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public MissingSampleException(String individualId) {
    super(format("No sample found for sampleId '%s'", individualId));
  }
}
