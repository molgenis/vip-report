package org.molgenis.vcf.report.fasta;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.*;

import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import org.springframework.stereotype.Component;

@Component
public class VcfIntervalCalculator {

  public List<ContigInterval> calculate(VCFHeader vcfHeader, Iterable<VariantContext> variantContexts, int flanking) {
    return calculate(vcfHeader, variantContexts, flanking, null);
  }

  public List<ContigInterval> calculate(
      VCFHeader vcfHeader, Iterable<VariantContext> variantContexts, int flanking, String sampleId) {
    Map<String, Integer> contigLengthMap = createContigLengthMap(vcfHeader.getContigLines());
    Map<String, List<ContigInterval>> intervalMap =
        computeIntervalMap(variantContexts, flanking, contigLengthMap, sampleId);
    List<ContigInterval> intervals = new ArrayList<>();
    intervalMap.forEach((key, value) -> intervals.addAll(mergeIntervals(value)));
    return intervals;
  }

  private Map<String, Integer> createContigLengthMap(List<VCFContigHeaderLine> contigLines) {
    Map<String, Integer> contigLengthMap = new HashMap<>();
    contigLines.forEach(contigLine -> {
      String lengthStr = contigLine.getGenericFields().get("length");
      if(lengthStr != null) {
        int length = Integer.parseInt(lengthStr);
        contigLengthMap.put(contigLine.getID(), length);
      }
    });
    return contigLengthMap;
  }

  private static Map<String, List<ContigInterval>> computeIntervalMap(Iterable<VariantContext> variantContexts, int flanking, Map<String, Integer> contigLengthMap, String sampleId) {
    Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
    for (VariantContext variantContext : variantContexts) {
      if (includeVariantContext(sampleId, variantContext)) {
        String contig = variantContext.getContig();
        int pos = variantContext.getStart();
        int start = Math.max(pos - flanking, 1);
        Integer contigLength = contigLengthMap.get(contig);
        int stop = contigLength != null ? Math.min(pos + flanking, contigLength) : pos + flanking;
        ContigInterval contigInterval = new ContigInterval(contig, start, stop);
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
