package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InvalidSamplePhenotypesExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Invalid phenotype argument: 'test', valid example: 'sample1/phenotype1;phenotype2,sample2/phenotype1'",
        new InvalidSamplePhenotypesException("test").getMessage());
  }
}
