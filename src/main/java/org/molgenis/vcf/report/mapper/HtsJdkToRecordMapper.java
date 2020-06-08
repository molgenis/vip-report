package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.RecordSample;
import org.molgenis.vcf.report.model.Sample;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

/**
 * @see VariantContext
 * @see Record
 */
@Component
public class HtsJdkToRecordMapper {

  private final HtsJdkToInfoMapper htsJdkToInfoMapper;
  private final HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper;

  public HtsJdkToRecordMapper(
      HtsJdkToInfoMapper htsJdkToInfoMapper,
      HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper) {
    this.htsJdkToInfoMapper = requireNonNull(htsJdkToInfoMapper);
    this.htsJdkToRecordSampleMapper = requireNonNull(htsJdkToRecordSampleMapper);
  }

  public Record map(VCFHeader vcfHeader, VariantContext variantContext, List<Sample> samples) {
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

    Info info = htsJdkToInfoMapper.map(vcfHeader, variantContext.getAttributes());

    List<RecordSample> recordSamples;
    if (!samples.isEmpty()) {
      recordSamples = new ArrayList<>(samples.size());

      List<@NonNull String> sampleNames = samples.stream().map(Sample::getPerson).map(Person::getIndividualId).collect(toList());
      for (Genotype genotype : variantContext.getGenotypesOrderedBy(sampleNames)) {
        //Genotype can be null if PED input contains samples that or not in the VCF
        if (genotype != null) {
          RecordSample recordSample = htsJdkToRecordSampleMapper.map(genotype);
          recordSamples.add(recordSample);
        }
      }
    } else {
      recordSamples = emptyList();
    }

    return new Record(
        contig,
        start,
        ids,
        referenceAllele,
        alternateAlleles,
        quality,
        filters,
        info,
        recordSamples);
  }
}
