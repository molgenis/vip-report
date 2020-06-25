package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.model.Format;
import org.molgenis.vcf.report.model.Genotype;
import org.molgenis.vcf.report.model.RecordSample;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.springframework.stereotype.Component;

/**
 * @see htsjdk.variant.variantcontext.Genotype
 * @see RecordSample
 */
@Component
public class HtsJdkToRecordSampleMapper extends HtsJdkToCompoundMapper<Format> {

  private final HtsJdkToGenotypeTypeMapper htsJdkGenotypeTypeMapper;

  public HtsJdkToRecordSampleMapper(HtsJdkToGenotypeTypeMapper htsJdkGenotypeTypeMapper) {
    this.htsJdkGenotypeTypeMapper = requireNonNull(htsJdkGenotypeTypeMapper);
  }

  public RecordSample map(
      List<FormatMetadata> formatMetadataList,
      htsjdk.variant.variantcontext.Genotype htsJdkGenotype) {
    Genotype genotype = mapGenotype(htsJdkGenotype);
    Map<String, Object> dataMap = mapData(formatMetadataList, htsJdkGenotype);
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

  private Map<String, Object> mapData(
      List<FormatMetadata> formatMetadataList,
      htsjdk.variant.variantcontext.Genotype htsJdkGenotype) {
    @SuppressWarnings("unchecked")
    Format format =
        map(
            (List<CompoundMetadata<Format>>) (List<?>) formatMetadataList,
            htsJdkGenotype.getExtendedAttributes());

    if (htsJdkGenotype.hasGQ()) {
      format.put("GQ", htsJdkGenotype.getGQ());
    }
    if (htsJdkGenotype.hasAD()) {
      format.put("AD", Arrays.stream(htsJdkGenotype.getAD()).boxed().collect(toList()));
    }
    if (htsJdkGenotype.hasDP()) {
      format.put("DP", htsJdkGenotype.getDP());
    }
    if (htsJdkGenotype.hasPL()) {
      format.put("PL", Arrays.stream(htsJdkGenotype.getPL()).boxed().collect(toList()));
    }

    return format;
  }

  @Override
  public Format map(
      List<CompoundMetadata<Format>> compoundMetadataList, Map<String, Object> attributes) {
    Format format = new Format();
    attributes.forEach(
        (key, value) -> {
          CompoundMetadata<Format> compoundMetadata =
              compoundMetadataList.stream()
                  .filter(aCompoundMetadata -> aCompoundMetadata.getId().equals(key))
                  .findFirst()
                  .orElse(null);
          Object mappedValue = mapAttribute(compoundMetadata, value);
          format.put(key, mappedValue);
        });
    return format;
  }
}
