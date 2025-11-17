package org.molgenis.vcf.report.utils;

import static java.lang.String.format;

public class UnexpectedCategoryException extends RuntimeException {
    public UnexpectedCategoryException(String field, String stringValue) {
        super(
                format(
                        "Unexpected value '%s' for categorical field '%s'", stringValue, field));
    }
}
