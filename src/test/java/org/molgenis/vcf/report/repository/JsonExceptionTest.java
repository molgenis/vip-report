package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JsonExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Error converting data to json: 'TEST'",
        new JsonException("TEST").getMessage());
  }
}