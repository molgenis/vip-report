package org.molgenis.vcf.report.utils;

import static java.lang.String.format;

public class InvalidPedException extends RuntimeException {
  private static final String MESSAGE = "Invalid PED line, expected 6 columns on line: %s";
  private final String argument;

  public InvalidPedException(String argument) {
    this.argument = argument;
  }

  @Override
  public String getMessage() {
    return format(
        MESSAGE,
        argument);
  }
}
