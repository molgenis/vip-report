package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.fasta.ContigInterval;

class IntervalUtilsTest {

  @Test
  void mergeIntervalsEmpty() {
    assertEquals(Arrays.asList(), IntervalUtils.mergeIntervals(Arrays.asList()));
  }

  @Test
  void mergeIntervalsOne() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 3)),
        IntervalUtils.mergeIntervals(Arrays.asList(new ContigInterval("x", 1, 3))));
  }

  @Test
  void mergeIntervalsNoOverlap() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 7))));
  }

  @Test
  void mergeIntervalsOverlap() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 4)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 2, 4))));
  }

  @Test
  void mergeIntervalsOverlapBoundary() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 5)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 3, 5))));
  }

  @Test
  void mergeIntervalsOverlapAdjacent() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 6)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 4, 6))));
  }

  @Test
  void mergeIntervalsMultipleLastOverlap() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 3), new ContigInterval("x", 5, 8)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 5, 7),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleFirstOverlap() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 4), new ContigInterval("x", 6, 8)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 6, 8))));
  }

  @Test
  void mergeIntervalsMultipleAllOverlap() {
    assertEquals(
        Arrays.asList(new ContigInterval("x", 1, 6)),
        IntervalUtils.mergeIntervals(
            Arrays.asList(
                new ContigInterval("x", 1, 3),
                new ContigInterval("x", 2, 4),
                new ContigInterval("x", 4, 6))));
  }
}
