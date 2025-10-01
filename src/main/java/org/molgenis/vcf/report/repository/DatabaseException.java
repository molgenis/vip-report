package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(
                format(
                        "Error while communicating with the database: '%s'", message));
    }
}
