package org.molgenis.vcf.report.generator;

import static java.lang.String.format;

import java.io.Serial;

public class InvalidHpoLineException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public InvalidHpoLineException(String line, String filename) {
    super(
        format(
            "Invalid HPO line '%s' in file '%s'. Expected format id<tab>label<tab>description.",
            line, filename));
  }
}
