package org.molgenis.vcf.report.repository;

import static java.lang.String.format;

public class JsonException extends RuntimeException {

  public JsonException(String message) {
    super(format("Error converting data to json: '%s'", message));
  }
}
