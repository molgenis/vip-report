package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import java.util.List;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkMapper {

  private final HtsJdkToRecordsMapper htsJdkToRecordsMapper;
  private final HtsJdkToPersonsMapper htsJdkToPersonsMapper;

  public HtsJdkMapper(
      HtsJdkToRecordsMapper htsJdkToRecordsMapper, HtsJdkToPersonsMapper htsJdkToPersonsMapper) {
    this.htsJdkToRecordsMapper = requireNonNull(htsJdkToRecordsMapper);
    this.htsJdkToPersonsMapper = requireNonNull(htsJdkToPersonsMapper);
  }

  public Items<Record> mapRecords(
      Iterable<VariantContext> variantContexts, int maxRecords, List<Person> persons) {
    return htsJdkToRecordsMapper.map(variantContexts, maxRecords, persons);
  }

  public Items<Person> mapSamples(VCFHeader vcfHeader, int maxSamples) {
    return htsJdkToPersonsMapper.map(vcfHeader, maxSamples);
  }
}
