package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VcfIntervalCalculatorTest {

  private VcfIntervalCalculator vcfIntervalCalculator;

  @BeforeEach
  void setUpBeforeEach() {
    vcfIntervalCalculator = new VcfIntervalCalculator();
  }

  @Test
  void calculate() {
    VariantContext variantContext0 = mock(VariantContext.class);
    when(variantContext0.getContig()).thenReturn("1");
    when(variantContext0.getStart()).thenReturn(100);
    VariantContext variantContext1 = mock(VariantContext.class);
    when(variantContext1.getContig()).thenReturn("1");
    when(variantContext1.getStart()).thenReturn(110);
    assertEquals(
        List.of(new ContigInterval("1", 75, 135)),
        vcfIntervalCalculator.calculate(List.of(variantContext0, variantContext1), 25));
  }

  @Test
  void mergeIntervalsEmpty() {
    assertEquals(List.of(), VcfIntervalCalculator.mergeIntervals(List.of()));
  }

  @Test
  void mergeIntervalsOne() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3)),
        VcfIntervalCalculator.mergeIntervals(List.of(new ContigInterval("x", 1, 3))));
  }

  @Test
  void mergeIntervalsNoOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7))));
  }

  @Test
  void mergeIntervalsOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 4)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 2, 4))));
  }

  @Test
  void mergeIntervalsOverlapBoundary() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 5)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 3, 5))));
  }

  @Test
  void mergeIntervalsOverlapAdjacent() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 6)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 4, 6))));
  }

  @Test
  void mergeIntervalsMultipleLastOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 8)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 5, 7),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleFirstOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 4), new ContigInterval("x", 6, 8)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleAllOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 6)),
        VcfIntervalCalculator.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 4, 6))));
  }
}
