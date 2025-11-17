package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InvalidIntegerExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Invalid integer value 'NotAnInt' for option 'OPT_TEST', value must be 0 or more.",
        new InvalidIntegerException("OPT_TEST", "NotAnInt").getMessage());
  }
}
