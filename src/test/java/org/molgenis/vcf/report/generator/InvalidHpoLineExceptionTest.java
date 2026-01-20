package org.molgenis.vcf.report.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InvalidHpoLineExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Invalid HPO line 'HPO:1234567\tMyLabel' in file 'hpo.tsv'. Expected format id<tab>label<tab>description.",
        new InvalidHpoLineException("HPO:1234567\tMyLabel", "hpo.tsv").getMessage());
  }
}
