package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

public class MissingDecisionTreeException extends RuntimeException {
  public MissingDecisionTreeException(String field, String resource) {
    super(format("Field '%s' is CATEGORICAL, but required parameter '%s' for this to work is missing.", field, resource));
  }
}
