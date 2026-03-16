package org.molgenis.vcf.report.utils;

import static java.lang.String.format;

import java.io.Serial;

public class UnexpectedCategoryException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public UnexpectedCategoryException(String field, String stringValue) {
    super(format("Unexpected value '%s' for categorical field '%s'", stringValue, field));
  }
}
