package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import java.io.Serial;
import java.util.Set;

public class InvalidHpoException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public InvalidHpoException(String val, Set<String> terms) {
    super(
        format(
            "HPO value '%s' is not part of the provided HPO terms '%s'.",
            val, String.join(",", terms)));
  }
}
