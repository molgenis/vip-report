package org.molgenis.vcf.report.fasta;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class VcfIntervalCalculator {

  public List<ContigInterval> calculate(Iterable<VariantContext> variantContexts, int flanking) {
    return calculate(variantContexts, flanking, null);
  }

  public List<ContigInterval> calculate(
      Iterable<VariantContext> variantContexts, int flanking, String sampleId) {
    Map<String, List<ContigInterval>> intervalMap =
        computeIntervalMap(variantContexts, flanking, sampleId);
    List<ContigInterval> intervals = new ArrayList<>();
    intervalMap.forEach((key, value) -> intervals.addAll(mergeIntervals(value)));
    return intervals;
  }

  private static Map<String, List<ContigInterval>> computeIntervalMap(
      Iterable<VariantContext> variantContexts, int flanking, String sampleId) {
    Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
    for (VariantContext variantContext : variantContexts) {
      if (includeVariantContext(sampleId, variantContext)) {
        String contig = variantContext.getContig();
        int pos = variantContext.getStart();
        ContigInterval contigInterval = new ContigInterval(contig, pos - flanking, pos + flanking);
        intervalMap.computeIfAbsent(contig, k -> new ArrayList<>()).add(contigInterval);
      }
    }
    return intervalMap;
  }

  private static boolean includeVariantContext(String sampleId, VariantContext variantContext) {
    return sampleId == null
        || (variantContext.hasGenotype(sampleId)
            && variantContext.getGenotype(sampleId).isCalled());
  }

  /** package-private for testability */
  static List<ContigInterval> mergeIntervals(List<ContigInterval> intervals) {
    List<ContigInterval> mergedIntervals = new ArrayList<>();
    if (intervals.size() < 2) {
      mergedIntervals = intervals;
    } else {
      ContigInterval interval = intervals.get(0);
      for (int i = 1; i < intervals.size(); ++i) {
        ContigInterval nextInterval = intervals.get(i);
        if (nextInterval.getStart() <= interval.getStop() + 1) {
          interval =
              new ContigInterval(interval.getContig(), interval.getStart(), nextInterval.getStop());
        } else {
          mergedIntervals.add(interval);
          interval = nextInterval;
        }
      }
      mergedIntervals.add(interval);
    }
    return mergedIntervals;
  }
}
