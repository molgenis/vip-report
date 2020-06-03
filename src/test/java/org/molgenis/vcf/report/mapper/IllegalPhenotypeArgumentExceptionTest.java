package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.utils.InvalidSamplePhenotypesException;

class IllegalPhenotypeArgumentExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Illegal phenotype 'test' phenotypes must be specified in CURIE format.",new IllegalPhenotypeArgumentException("test").getMessage());
  }
}