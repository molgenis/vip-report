package org.molgenis.vcf.report.genes;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.fasta.VariantIntervalCalculator;
import org.springframework.stereotype.Component;

@Component
public class GenesFilterFactory {

  private final VariantIntervalCalculator variantIntervalCalculator;

  GenesFilterFactory(VariantIntervalCalculator variantIntervalCalculator) {
    this.variantIntervalCalculator = requireNonNull(variantIntervalCalculator);
  }

  public GenesFilter create(Path genesFile) {
    return new GenesFilter(variantIntervalCalculator, genesFile);
  }
}
