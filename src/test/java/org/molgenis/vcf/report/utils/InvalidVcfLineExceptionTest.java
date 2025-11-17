package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InvalidVcfLineExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "VCF line with to little columns detected: 1\t2\t1",
        new InvalidVcfLineException("1\t2\t1").getMessage());
  }
}
