package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class VcfFastaSlicerFactory {

  private final FastaSlicerFactory fastaSlicerFactory;
  private final VcfIntervalCalculator vcfIntervalCalculator;

  VcfFastaSlicerFactory(FastaSlicerFactory fastaSlicerFactory,
      VcfIntervalCalculator vcfIntervalCalculator) {
    this.fastaSlicerFactory = requireNonNull(fastaSlicerFactory);
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
  }

  public VcfFastaSlicer create(Path fastaGzPath) {
    FastaSlicer fastaSlicer = fastaSlicerFactory.create(fastaGzPath);
    return new VcfFastaSlicer(fastaSlicer, vcfIntervalCalculator);
  }
}
