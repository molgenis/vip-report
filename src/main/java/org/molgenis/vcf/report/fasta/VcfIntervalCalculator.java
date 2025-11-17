package org.molgenis.vcf.report.fasta;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class VcfIntervalCalculator {

  public Map<String, List<ContigInterval>> calculate(
      VCFHeader vcfHeader, Iterator<VariantContext> vcfIterator, int flanking) {
    return calculate(vcfHeader, vcfIterator, flanking, null);
  }

  public Map<String, List<ContigInterval>> calculate(
      VCFHeader vcfHeader, Iterator<VariantContext> vcfIterator, int flanking, String sampleId) {
    Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
    Map<String, Integer> contigLengthMap = createContigLengthMap(vcfHeader.getContigLines());
    vcfIterator.forEachRemaining(
        variantContext -> {
          if (includeVariantContext(sampleId, variantContext)) {
            String contig = variantContext.getContig();
            int pos = variantContext.getStart();
            int start = Math.max(pos - flanking, 1);
            Integer contigLength = contigLengthMap.get(contig);
            int stop =
                contigLength != null ? Math.min(pos + flanking, contigLength) : pos + flanking;
            ContigInterval contigInterval = new ContigInterval(contig, start, stop);
            intervalMap.computeIfAbsent(contig, k -> new ArrayList<>()).add(contigInterval);
          }
        });
    return intervalMap;
  }

  private Map<String, Integer> createContigLengthMap(List<VCFContigHeaderLine> contigLines) {
    Map<String, Integer> contigLengthMap = new HashMap<>();
    contigLines.forEach(
        contigLine -> {
          String lengthStr = contigLine.getGenericFields().get("length");
          if (lengthStr != null) {
            int length = Integer.parseInt(lengthStr);
            contigLengthMap.put(contigLine.getID(), length);
          }
        });
    return contigLengthMap;
  }

  private static boolean includeVariantContext(String sampleId, VariantContext variantContext) {
    return sampleId == null
        || (variantContext.hasGenotype(sampleId)
            && variantContext.getGenotype(sampleId).isCalled());
  }
}
