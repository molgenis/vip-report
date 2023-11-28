package org.molgenis.vcf.report.genes;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import org.molgenis.vcf.report.fasta.CramIntervalCalculator;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.springframework.stereotype.Component;

@Component
public class GenesFilterFactory {

  private final CramIntervalCalculator cramIntervalCalculator;
  private final VcfIntervalCalculator vcfIntervalCalculator;

  GenesFilterFactory(VcfIntervalCalculator vcfIntervalCalculator, CramIntervalCalculator cramIntervalCalculator) {
    this.cramIntervalCalculator = requireNonNull(cramIntervalCalculator);
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
  }

  public GenesFilter create(Path genesFile) {
    return new GenesFilter(vcfIntervalCalculator, cramIntervalCalculator, genesFile);
  }
}
