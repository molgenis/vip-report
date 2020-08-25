package org.molgenis.vcf.report;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class InvalidIntegerException extends IllegalArgumentException {
  private static final String INVALID_INTEGER_MESSAGE =
      "Invalid integer value '%s' for option '%s', value must be 0 or more.";
  private final String stringValue;
  private final String opt;

  public InvalidIntegerException(String opt, String stringValue) {
    this.opt = requireNonNull(opt);
    this.stringValue = requireNonNull(stringValue);
  }

  @Override
  public String getMessage() {
    return format(INVALID_INTEGER_MESSAGE, stringValue, opt);
  }
}
