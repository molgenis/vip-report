package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import org.molgenis.vcf.report.generator.SampleSettings;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CramFastaSlicer {

  private final FastaSlicer fastaSlicer;
  private final CramIntervalCalculator cramIntervalCalculator;

  public CramFastaSlicer(FastaSlicer fastaSlicer, CramIntervalCalculator cramIntervalCalculator) {
    this.cramIntervalCalculator = requireNonNull(cramIntervalCalculator);
    this.fastaSlicer = requireNonNull(fastaSlicer);
  }

  public List<FastaSlice> generate(Map<String, SampleSettings.CramPath> crampaths, Path reference) {
    List<ContigInterval> intervals = cramIntervalCalculator.calculate(crampaths, reference);
    return intervals.stream().map(fastaSlicer::slice).collect(toList());
  }
}
