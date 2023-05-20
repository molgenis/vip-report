package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;

import java.util.List;

public class VcfFastaSlicer {

  private final FastaSlicer fastaSlicer;
  private final VcfIntervalCalculator vcfIntervalCalculator;

  public VcfFastaSlicer(FastaSlicer fastaSlicer, VcfIntervalCalculator vcfIntervalCalculator) {
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
    this.fastaSlicer = requireNonNull(fastaSlicer);
  }

  public List<FastaSlice> generate(VCFHeader vcfHeader, Iterable<VariantContext> variants, int flanking) {
    List<ContigInterval> intervals = vcfIntervalCalculator.calculate(vcfHeader, variants, flanking);
    return intervals.stream().map(fastaSlicer::slice).collect(toList());
  }
}
