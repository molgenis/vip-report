package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class VcfFastaSlicerFactory {

  private final FastaSlicerFactory fastaSlicerFactory;

  VcfFastaSlicerFactory(FastaSlicerFactory fastaSlicerFactory) {
    this.fastaSlicerFactory = requireNonNull(fastaSlicerFactory);
  }

  public VariantFastaSlicer create(Path fastaGzPath) {
    FastaSlicer fastaSlicer = fastaSlicerFactory.create(fastaGzPath);
    return new VariantFastaSlicer(fastaSlicer);
  }
}
