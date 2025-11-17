package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnexpectedCategoryExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Unexpected value 'VALUE' for categorical field 'TEST'",
        new UnexpectedCategoryException("TEST", "VALUE").getMessage());
  }
}