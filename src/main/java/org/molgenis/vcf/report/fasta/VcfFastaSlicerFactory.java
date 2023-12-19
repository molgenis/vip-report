package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class VcfFastaSlicerFactory {

  private final FastaSlicerFactory fastaSlicerFactory;
  private final VariantIntervalCalculator variantIntervalCalculator;

  VcfFastaSlicerFactory(FastaSlicerFactory fastaSlicerFactory,
                        VariantIntervalCalculator variantIntervalCalculator) {
    this.fastaSlicerFactory = requireNonNull(fastaSlicerFactory);
    this.variantIntervalCalculator = requireNonNull(variantIntervalCalculator);
  }

  public VariantFastaSlicer create(Path fastaGzPath) {
    FastaSlicer fastaSlicer = fastaSlicerFactory.create(fastaGzPath);
    return new VariantFastaSlicer(fastaSlicer, variantIntervalCalculator);
  }
}
