package org.molgenis.vcf.report.bam;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;

public class VcfBamSlicer {
  private final BamSlicer bamSlicer;
  private final VcfIntervalCalculator vcfIntervalCalculator;

  public VcfBamSlicer(BamSlicer bamSlicer, VcfIntervalCalculator vcfIntervalCalculator) {
    this.bamSlicer = requireNonNull(bamSlicer);
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
  }

  public BamSlice generate(Iterable<VariantContext> variants, int flanking, String sampleId) {
    List<ContigInterval> intervals = vcfIntervalCalculator.calculate(variants, flanking, sampleId);
    return bamSlicer.slice(intervals);
  }
}
