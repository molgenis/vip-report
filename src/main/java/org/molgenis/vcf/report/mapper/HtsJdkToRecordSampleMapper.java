package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import org.molgenis.vcf.report.model.Genotype;
import org.molgenis.vcf.report.model.RecordSample;
import org.springframework.stereotype.Component;

/**
 * @see htsjdk.variant.variantcontext.Genotype
 * @see RecordSample
 */
@Component
public class HtsJdkToRecordSampleMapper {

  private final HtsJdkToGenotypeTypeMapper htsJdkGenotypeTypeMapper;

  public HtsJdkToRecordSampleMapper(HtsJdkToGenotypeTypeMapper htsJdkGenotypeTypeMapper) {
    this.htsJdkGenotypeTypeMapper = requireNonNull(htsJdkGenotypeTypeMapper);
  }

  public RecordSample map(htsjdk.variant.variantcontext.Genotype genotype) {
    return new RecordSample(mapGenotype(genotype));
  }

  private Genotype mapGenotype(htsjdk.variant.variantcontext.Genotype genotype) {
    List<String> alleles =
        genotype.getAlleles().stream()
            .map(htsjdk.variant.variantcontext.Allele::getDisplayString)
            .collect(toList());
    boolean phased = genotype.isPhased();

    Genotype.Type type = htsJdkGenotypeTypeMapper.map(genotype.getType());

    return new Genotype(alleles, phased, type);
  }
}
