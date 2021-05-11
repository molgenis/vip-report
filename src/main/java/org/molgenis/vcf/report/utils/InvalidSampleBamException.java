package org.molgenis.vcf.report.utils;

import static java.lang.String.format;

public class InvalidSampleBamException extends RuntimeException {

  private static final String MESSAGE =
      "Invalid bam argument: '%s', valid example: 'sample0=/path/to/0.bam,sample1=/path/to/1.bam'";
  private final String argument;

  public InvalidSampleBamException(String argument) {
    this.argument = argument;
  }

  @Override
  public String getMessage() {
    return format(MESSAGE, argument);
  }
}
