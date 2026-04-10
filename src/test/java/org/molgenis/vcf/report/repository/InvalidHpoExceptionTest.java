package org.molgenis.vcf.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InvalidHpoExceptionTest {
  @Test
  void getMessage() {
    Set<String> terms = new LinkedHashSet();
    terms.add("term1");
    terms.add("term2");
    assertEquals(
        "HPO value 'TEST' is not part of the provided HPO terms 'term1,term2'.",
        new InvalidHpoException("TEST", terms).getMessage());
  }
}
