package org.molgenis.vcf.report.utils;

import static java.lang.String.format;

import java.io.Serial;

public class InvalidVcfLineException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidVcfLineException(String line) {
    super(format("VCF line with to little columns detected: %s", line));
  }
}
