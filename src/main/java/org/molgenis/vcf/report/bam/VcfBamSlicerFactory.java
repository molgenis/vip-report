package org.molgenis.vcf.report.bam;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.springframework.stereotype.Component;

@Component
public class VcfBamSlicerFactory {
  private final BamSlicerFactory bamSlicerFactory;
  private final VcfIntervalCalculator vcfIntervalCalculator;

  VcfBamSlicerFactory(
      BamSlicerFactory bamSlicerFactory, VcfIntervalCalculator vcfIntervalCalculator) {
    this.bamSlicerFactory = requireNonNull(bamSlicerFactory);
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
  }

  public VcfBamSlicer create(Path bamPath) {
    BamSlicer bamSlicer = bamSlicerFactory.create(bamPath);
    return new VcfBamSlicer(bamSlicer, vcfIntervalCalculator);
  }
}
