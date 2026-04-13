package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import java.io.Serial;

public class MissingDecisionTreeException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public MissingDecisionTreeException(String field, String resource) {
    super(
        format(
            "Field '%s' is CATEGORICAL, but required parameter '%s' for this to work is missing.",
            field, resource));
  }
}
