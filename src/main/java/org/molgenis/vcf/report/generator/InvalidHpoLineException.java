package org.molgenis.vcf.report.generator;

import static java.lang.String.format;

public class InvalidHpoLineException extends RuntimeException {
  public InvalidHpoLineException(String line, String filename) {
    super(
        format(
            "Invalid HPO line '%s' in file '%s'. Expected format id<tab>label<tab>description.", line, filename));
  }
}
