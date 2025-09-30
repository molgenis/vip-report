package org.molgenis.vcf.report.genes;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class GenesFilterFactory {

  public GenesFilter create(Path genesFile) {
    return new GenesFilter(genesFile);
  }
}
