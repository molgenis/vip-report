package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

public class DatabaseException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public DatabaseException(@Nullable String message, String operation) {
    super(
        message != null
            ? format(
                "Error while communicating with the database: '%s' for operation '%s'",
                message, operation)
            : format("Error while communicating with the database for operation '%s'", operation));
  }
}
