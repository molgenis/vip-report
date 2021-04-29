package org.molgenis.vcf.report.genes;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.springframework.stereotype.Component;

@Component
public class GenesFilterFactory {

  private final VcfIntervalCalculator vcfIntervalCalculator;

  GenesFilterFactory(VcfIntervalCalculator vcfIntervalCalculator) {
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
  }

  public GenesFilter create(Path genesFile) {
    return new GenesFilter(vcfIntervalCalculator, genesFile);
  }
}
