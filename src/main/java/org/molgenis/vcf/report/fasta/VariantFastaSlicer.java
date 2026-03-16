package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.Nullable;
import org.molgenis.vcf.report.model.Bytes;

public class VariantFastaSlicer {
  private final FastaSlicer fastaSlicer;

  public VariantFastaSlicer(FastaSlicer fastaSlicer) {
    this.fastaSlicer = requireNonNull(fastaSlicer);
  }

  public @Nullable Map<String, Bytes> generate(
      List<ContigInterval> contigIntervals, @Nullable Path referencePath) {
    Map<String, Bytes> fastaGzMap;
    if (referencePath != null) {
      List<FastaSlice> fastaGzSlices;
      fastaGzSlices = contigIntervals.stream().map(fastaSlicer::slice).toList();
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
