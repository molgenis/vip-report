package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import org.jspecify.annotations.Nullable;

public class JsonException extends RuntimeException {
  public JsonException(@Nullable String message) {
    super(
        message != null
            ? format("Error converting data to json: '%s'", message)
            : "Error converting data to json");
  }
}
