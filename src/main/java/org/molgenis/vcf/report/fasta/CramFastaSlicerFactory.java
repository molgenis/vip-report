package org.molgenis.vcf.report.fasta;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Component
public class CramFastaSlicerFactory {

  private final FastaSlicerFactory fastaSlicerFactory;
  private final CramIntervalCalculator cramIntervalCalculator;

  CramFastaSlicerFactory(FastaSlicerFactory fastaSlicerFactory,
                         CramIntervalCalculator cramIntervalCalculator) {
    this.fastaSlicerFactory = requireNonNull(fastaSlicerFactory);
    this.cramIntervalCalculator = requireNonNull(cramIntervalCalculator);
  }

  public CramFastaSlicer create(Path fastaGzPath) {
    FastaSlicer fastaSlicer = fastaSlicerFactory.create(fastaGzPath);
    return new CramFastaSlicer(fastaSlicer, cramIntervalCalculator);
  }
}
