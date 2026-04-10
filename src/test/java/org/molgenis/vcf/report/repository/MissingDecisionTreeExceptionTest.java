package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MissingDecisionTreeExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Field 'VIPC_TEST' is CATEGORICAL, but required parameter '--test_param' for this to work is missing.",
        new MissingDecisionTreeException("VIPC_TEST", "--test_param").getMessage());
  }
}
