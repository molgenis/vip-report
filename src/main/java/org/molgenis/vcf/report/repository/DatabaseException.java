package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import org.jspecify.annotations.Nullable;

public class DatabaseException extends RuntimeException {
  public DatabaseException(@Nullable String message, String operation) {
    super(
        message != null
            ? format(
                "Error while communicating with the database: '%s' for operation '%s'",
                message, operation)
            : format("Error while communicating with the database for operation '%s'", operation));
  }
}
