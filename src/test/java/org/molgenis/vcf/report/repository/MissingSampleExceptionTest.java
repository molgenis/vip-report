package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MissingSampleExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "No sample found for sampleId 'TEST'",
        new MissingSampleException("TEST").getMessage());
  }
}