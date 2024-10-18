package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFIterator;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.model.Bytes;

import java.nio.file.Path;
import java.util.*;

public class VariantFastaSlicer {
  private final FastaSlicer fastaSlicer;
  private final VariantIntervalCalculator variantIntervalCalculator;

  public VariantFastaSlicer(FastaSlicer fastaSlicer, VariantIntervalCalculator variantIntervalCalculator) {
    this.fastaSlicer = requireNonNull(fastaSlicer);
    this.variantIntervalCalculator = variantIntervalCalculator;
  }

  public Map<String, Bytes> generate(VCFIterator vcfIterator, Map<String, SampleSettings.CramPath> cramPaths, Path referencePath) {
    Map<String, Bytes> fastaGzMap;
    if (referencePath != null) {
      List<FastaSlice> fastaGzSlices;
      List<ContigInterval> intervals = variantIntervalCalculator.calculate(vcfIterator, cramPaths, referencePath);
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
}
