package org.molgenis.vcf.report.utils;

import java.io.Serial;

import static java.lang.String.format;

public class InvalidSampleCramException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final String MESSAGE =
      "Invalid cram argument: '%s', valid example: 'sample0=/path/to/0.cram,sample1=/path/to/1.cram'";
  private final String argument;

  public InvalidSampleCramException(String argument) {
    this.argument = argument;
  }

  @Override
  public String getMessage() {
    return format(MESSAGE, argument);
  }
}
