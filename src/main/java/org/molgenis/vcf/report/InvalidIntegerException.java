package org.molgenis.vcf.report;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Serial;

public class InvalidIntegerException extends IllegalArgumentException {

  @Serial
  private static final long serialVersionUID = 1L;
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
