package org.molgenis.vcf.report.utils;

import java.io.Serial;

import static java.lang.String.format;

public class InvalidSampleBedmethylException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE =
            "Invalid bedmethyl argument: '%s', valid example: 'sample0=/path/to/0.bedmethyl,sample1=/path/to/1.bedmethyl'";
    private final String argument;

    public InvalidSampleBedmethylException(String argument) {
        this.argument = argument;
    }

    @Override
    public String getMessage() {
        return format(MESSAGE, argument);
    }
}
