package org.molgenis.vcf.report.mapper;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class IllegalPhenotypeArgumentException extends RuntimeException {
  private static final String MESSAGE =
      "Illegal phenotype '%s' phenotypes must be specified in CURIE (prefix:reference) format.";
  private final String argument;

  public IllegalPhenotypeArgumentException(String argument) {
    this.argument = requireNonNull(argument);
  }

  @Override
  public String getMessage() {
    return format(MESSAGE, argument);
  }
}
