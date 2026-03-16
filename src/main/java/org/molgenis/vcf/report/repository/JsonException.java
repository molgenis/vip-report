package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

public class JsonException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public JsonException(@Nullable String message) {
    super(
        message != null
            ? format("Error converting data to json: '%s'", message)
            : "Error converting data to json");
  }
}
