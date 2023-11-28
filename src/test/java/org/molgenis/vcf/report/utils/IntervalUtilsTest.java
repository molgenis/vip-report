package org.molgenis.vcf.report.utils;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.molgenis.vcf.report.utils.IntervalUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntervalUtilsTest {
  @Test
  void mergeIntervalsEmpty() {
    assertEquals(List.of(), IntervalUtils.mergeIntervals(List.of()));
  }

  @Test
  void mergeIntervalsOne() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3)),
            IntervalUtils.mergeIntervals(List.of(new ContigInterval("x", 1, 3))));
  }

  @Test
  void mergeIntervalsNoOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7)),
            IntervalUtils.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7))));
  }

  @Test
  void mergeIntervalsOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 4)),
            IntervalUtils.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 2, 4))));
  }

  @Test
  void mergeIntervalsOverlapBoundary() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 5)),
            IntervalUtils.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 3, 5))));
  }

  @Test
  void mergeIntervalsOverlapAdjacent() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 6)),
            IntervalUtils.mergeIntervals(
            List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 4, 6))));
  }

  @Test
  void mergeIntervalsMultipleLastOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 8)),
            IntervalUtils.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 5, 7),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleFirstOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 4), new ContigInterval("x", 6, 8)),
            IntervalUtils.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleAllOverlap() {
    assertEquals(
        List.of(new ContigInterval("x", 1, 6)),
            IntervalUtils.mergeIntervals(
            List.of(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 4, 6))));
  }
}
