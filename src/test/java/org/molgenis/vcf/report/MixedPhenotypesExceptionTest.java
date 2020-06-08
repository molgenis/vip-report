package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MixedPhenotypesExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Mixing general phenotypes for all samples and phenotypes per sample is not allowed.",
        new MixedPhenotypesException().getMessage());
  }
}