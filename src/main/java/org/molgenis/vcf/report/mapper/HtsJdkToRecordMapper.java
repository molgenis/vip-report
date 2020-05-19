package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.RecordSample;
import org.molgenis.vcf.report.model.Sample;
import org.springframework.stereotype.Component;

/**
 * @see VariantContext
 * @see Record
 */
@Component
public class HtsJdkToRecordMapper {

  private final HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper;

  public HtsJdkToRecordMapper(HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper) {
    this.htsJdkToRecordSampleMapper = requireNonNull(htsJdkToRecordSampleMapper);
  }

  public Record map(VariantContext variantContext, List<Sample> samples) {
    String contig = variantContext.getContig();
    if (contig == null) {
      throw new VcfParseException("Chromosome can't be empty");
    }

    int start = variantContext.getStart();
    String referenceAllele = variantContext.getReference().getDisplayString();
    List<String> alternateAlleles =
        variantContext.getAlternateAlleles().stream()
            .map(htsjdk.variant.variantcontext.Allele::getDisplayString)
            .collect(toList());

    Record record = new Record(contig, start, referenceAllele, alternateAlleles);

    if (variantContext.hasID()) {
      String id = variantContext.getID();
      List<String> ids = Arrays.asList(id.split(";", -1));
      record.setIdentifiers(ids);
    }

    if (variantContext.hasLog10PError()) {
      record.setQuality(variantContext.getPhredScaledQual());
    }
    if (variantContext.filtersWereApplied()) {
      Set<String> filterSet = variantContext.getFilters();
      List<String> filters = new ArrayList<>(filterSet);
      Collections.sort(filters);
      record.setFilterStatus(filters);
    }

    if (!samples.isEmpty()) {
      List<RecordSample> recordSamples = new ArrayList<>(samples.size());

      List<@NonNull String> sampleNames = samples.stream().map(Sample::getName).collect(toList());
      for (Genotype genotype : variantContext.getGenotypesOrderedBy(sampleNames)) {
        RecordSample recordSample = htsJdkToRecordSampleMapper.map(genotype);
        recordSamples.add(recordSample);
      }

      record.setRecordSamples(recordSamples);
    }

    return record;
  }
}
