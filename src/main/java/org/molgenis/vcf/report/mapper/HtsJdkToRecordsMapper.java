package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.List;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Sample;
import org.springframework.stereotype.Component;

/**
 * @see VariantContext
 * @see Record
 */
@Component
public class HtsJdkToRecordsMapper {

  private final HtsJdkToRecordMapper htsJdkToRecordMapper;

  public HtsJdkToRecordsMapper(HtsJdkToRecordMapper htsJdkToRecordMapper) {
    this.htsJdkToRecordMapper = requireNonNull(htsJdkToRecordMapper);
  }

  public Items<Record> map(Iterable<VariantContext> variantContexts, int maxNrRecords,
      List<Sample> samples) {
    List<Record> records = new ArrayList<>(maxNrRecords);
    long nrRecord = 0;
    for (VariantContext variantContext : variantContexts) {
      if (nrRecord < maxNrRecords) {
        Record record = htsJdkToRecordMapper.map(variantContext, samples);
        records.add(record);
      }
      ++nrRecord;
    }
    return new Items<>(records, nrRecord);
  }
}
