package org.molgenis.vcf.report.bedmethyl;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.fasta.VariantIntervalCalculator;
import org.springframework.stereotype.Component;

@Component
public class BedmethylFilterFactory {

  private final VariantIntervalCalculator variantIntervalCalculator;

  BedmethylFilterFactory(VariantIntervalCalculator variantIntervalCalculator) {
    this.variantIntervalCalculator = requireNonNull(variantIntervalCalculator);
  }

  public BedmethylFilter create(Path bedmethylFile) {
    return new BedmethylFilter(variantIntervalCalculator, bedmethylFile);
  }
}