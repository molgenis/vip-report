package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DatabaseExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Error while communicating with the database: 'TEST' for operation 'OPERATION'",
        new DatabaseException("TEST", "OPERATION").getMessage());
  }
}