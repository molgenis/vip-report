package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.vcf.report.utils.IntervalUtils.mergeIntervals;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.model.Bytes;

import java.nio.file.Path;
import java.util.*;

public class VcfFastaSlicer {
  public static final int FLANKING = 250;
  private final FastaSlicer fastaSlicer;
  private final VcfIntervalCalculator vcfIntervalCalculator;
  private final CramIntervalCalculator cramIntervalCalculator;

  public VcfFastaSlicer(FastaSlicer fastaSlicer, VcfIntervalCalculator vcfIntervalCalculator, CramIntervalCalculator cramIntervalCalculator) {
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
    this.fastaSlicer = requireNonNull(fastaSlicer);
    this.cramIntervalCalculator = cramIntervalCalculator;
  }

  public Map<String, Bytes> generate(VCFFileReader vcfFileReader, Map<String, SampleSettings.CramPath> cramPaths, Path referencePath) {
    Map<String, Bytes> fastaGzMap;
    if (referencePath != null) {
      List<FastaSlice> fastaGzSlices;
      Map<String, List<ContigInterval>> cramIntervals;
      List<ContigInterval> intervals;
      if(cramPaths != null && !cramPaths.isEmpty()) {
        Map<String, List<ContigInterval>> vcfIntervals = vcfIntervalCalculator.computeIntervalMap(vcfFileReader.getHeader(), vcfFileReader, FLANKING, null);
        cramIntervals = cramIntervalCalculator.computeIntervalMap(cramPaths, referencePath);
        intervals = mergeIntervalLists(cramIntervals, vcfIntervals);
      }else{
        intervals = vcfIntervalCalculator.calculate(vcfFileReader.getHeader(), vcfFileReader, FLANKING);
      }
      fastaGzSlices = intervals.stream().map(fastaSlicer::slice).collect(toList());
      fastaGzMap = new LinkedHashMap<>();
      fastaGzSlices.forEach(
              fastaSlice -> {
                String key = getFastaSliceIdentifier(fastaSlice);
                fastaGzMap.put(key, new Bytes(fastaSlice.getFastaGz()));
              });
    } else {
      fastaGzMap = null;
    }
    return fastaGzMap;
  }

  private static String getFastaSliceIdentifier(FastaSlice fastaSlice) {
    ContigInterval interval = fastaSlice.getInterval();
    return interval.getContig() + ':' + interval.getStart() + '-' + interval.getStop();
  }
  private List<ContigInterval> mergeIntervalLists(Map<String, List<ContigInterval>> cramIntervalMap, Map<String, List<ContigInterval>> vcfIntervalMap) {
    List<ContigInterval> intervals = new ArrayList<>();
    Set<String> keys = new HashSet<>();
    keys.addAll(cramIntervalMap.keySet());
    keys.addAll(vcfIntervalMap.keySet());
    for(String key : keys){
      List<ContigInterval> vcfIntervals = vcfIntervalMap.containsKey(key) ? vcfIntervalMap.get(key) : Collections.emptyList();
      List<ContigInterval> cramIntervals = cramIntervalMap.containsKey(key) ? cramIntervalMap.get(key) : Collections.emptyList();
      List<ContigInterval> combinedIntervals = new ArrayList<>();
      combinedIntervals.addAll(vcfIntervals);
      combinedIntervals.addAll(cramIntervals);
      intervals.addAll(mergeIntervals(combinedIntervals));
    }
    return intervals;
  }
}
