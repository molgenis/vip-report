package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  public RecordSample map(htsjdk.variant.variantcontext.Genotype htsJdkGenotype) {
    Genotype genotype = mapGenotype(htsJdkGenotype);
    Map<String, Object> dataMap = mapData(htsJdkGenotype);
    return new RecordSample(genotype, dataMap);
  }

  private Genotype mapGenotype(htsjdk.variant.variantcontext.Genotype htsJdkGenotype) {
    Genotype genotype;
    if (htsJdkGenotype.isAvailable()) {
      List<String> alleles =
          htsJdkGenotype.getAlleles().stream()
              .map(htsjdk.variant.variantcontext.Allele::getDisplayString)
              .collect(toList());
      boolean phased = htsJdkGenotype.isPhased();

      Genotype.Type type = htsJdkGenotypeTypeMapper.map(htsJdkGenotype.getType());

      genotype = new Genotype(alleles, phased, type);
    } else {
      genotype = null;
    }
    return genotype;
  }

  private Map<String, Object> mapData(htsjdk.variant.variantcontext.Genotype htsJdkGenotype) {
    Map<String, Object> dataMap = new LinkedHashMap<>();
    if (htsJdkGenotype.hasGQ()) {
      dataMap.put("GQ", htsJdkGenotype.getGQ());
    }
    if (htsJdkGenotype.hasAD()) {
      dataMap.put("AD", Arrays.stream(htsJdkGenotype.getAD()).boxed().collect(toList()));
    }
    if (htsJdkGenotype.hasDP()) {
      dataMap.put("DP", htsJdkGenotype.getDP());
    }
    if (htsJdkGenotype.hasPL()) {
      dataMap.put("PL", Arrays.stream(htsJdkGenotype.getPL()).boxed().collect(toList()));
    }
    dataMap.putAll(htsJdkGenotype.getExtendedAttributes());
    return dataMap;
  }
}
