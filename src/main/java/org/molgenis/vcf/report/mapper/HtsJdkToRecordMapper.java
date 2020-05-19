package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
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

    List<String> ids;
    if (variantContext.hasID()) {
      String id = variantContext.getID();
      ids = Arrays.asList(id.split(";", -1));
    } else {
      ids = emptyList();
    }

    Double quality = variantContext.hasLog10PError() ? variantContext.getPhredScaledQual() : null;

    List<String> filters;
    if (variantContext.filtersWereApplied()) {
      Set<String> filterSet = variantContext.getFilters();
      filters = new ArrayList<>(filterSet);
      Collections.sort(filters);
    } else {
      filters = emptyList();
    }

    List<RecordSample> recordSamples;
    if (!samples.isEmpty()) {
      recordSamples = new ArrayList<>(samples.size());

      List<@NonNull String> sampleNames = samples.stream().map(Sample::getName).collect(toList());
      for (Genotype genotype : variantContext.getGenotypesOrderedBy(sampleNames)) {
        RecordSample recordSample = htsJdkToRecordSampleMapper.map(genotype);
        recordSamples.add(recordSample);
      }
    } else {
      recordSamples = emptyList();
    }

    return new Record(
        contig, start, ids, referenceAllele, alternateAlleles, quality, filters, recordSamples);
  }
}
