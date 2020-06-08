package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import java.util.List;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Sample;
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
      Iterable<VariantContext> variantContexts, int maxRecords, List<Sample> samples) {
    return htsJdkToRecordsMapper.map(variantContexts, maxRecords, samples);
  }

  public Items<Sample> mapSamples(VCFHeader vcfHeader, int maxSamples) {
    return htsJdkToPersonsMapper.map(vcfHeader, maxSamples);
  }
}
