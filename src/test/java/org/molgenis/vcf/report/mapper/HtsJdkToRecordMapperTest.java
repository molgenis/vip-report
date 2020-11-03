package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper.MISSING_PERSON_ID;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.AffectedStatus;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.Person;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.RecordSample;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.Sex;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;

@ExtendWith(MockitoExtension.class)
class HtsJdkToRecordMapperTest {
  @Mock private HtsJdkToInfoMapper htsJdkToInfoMapper;
  @Mock private HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper;
  private HtsJdkToRecordMapper htsJdkToRecordMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToRecordMapper = new HtsJdkToRecordMapper(htsJdkToInfoMapper, htsJdkToRecordSampleMapper);
  }

  @SuppressWarnings("unchecked")
  @Test
  void map() {
    String contig = "MyContig";
    int position = 123;
    String referenceAllele = "C";
    List<String> altAlleles = List.of("C", "T");

    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn(contig);
    when(variantContext.getStart()).thenReturn(position);
    when(variantContext.getReference()).thenReturn(Allele.REF_C);
    when(variantContext.getAlternateAlleles()).thenReturn(List.of(Allele.ALT_C, Allele.ALT_T));
    Map<String, Object> attributes = emptyMap();
    when(variantContext.getAttributes()).thenReturn(attributes);
    Info info = new Info();

    List<InfoMetadata> infoMetadataList = emptyList();
    List<FormatMetadata> formatMetadataList = emptyList();
    RecordsMetadata recordsMetadata = new RecordsMetadata(infoMetadataList, formatMetadataList);
    when(htsJdkToInfoMapper.map(
        (List<CompoundMetadata<Info>>) (List<?>) infoMetadataList, attributes))
        .thenReturn(info);

    Record record =
        new Record(
            contig,
            position,
            emptyList(),
            referenceAllele,
            altAlleles,
            null,
            emptyList(),
            new Info(),
            emptyList());
    Assertions.assertEquals(
        record, htsJdkToRecordMapper.map(recordsMetadata, variantContext, emptyList()));
  }

  @Test
  void mapMultipleSamples() {
    String contig = "MyContig";
    int position = 123;

    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn(contig);
    when(variantContext.getStart()).thenReturn(position);
    when(variantContext.getReference()).thenReturn(Allele.REF_C);
    when(variantContext.getAlternateAlleles()).thenReturn(List.of(Allele.ALT_C, Allele.ALT_T));

    Sample sample1 = Sample.builder().person(new Person(
        "family",
        "c",
        MISSING_PERSON_ID,
        MISSING_PERSON_ID,
        Sex.UNKNOWN_SEX,
        AffectedStatus.MISSING)).index(0).build();
    Sample sample2 = Sample.builder().person(new Person(
        "family",
        "b",
        MISSING_PERSON_ID,
        MISSING_PERSON_ID,
        Sex.UNKNOWN_SEX,
        AffectedStatus.MISSING)).index(1).build();
    Sample sample3 = Sample.builder().person(new Person(
        "family",
        "a",
        MISSING_PERSON_ID,
        MISSING_PERSON_ID,
        Sex.UNKNOWN_SEX,
        AffectedStatus.MISSING)).index(2).build();
    List<Sample> samples = new ArrayList<>();
    samples.add(sample2);
    samples.add(sample3);
    samples.add(sample1);
    htsJdkToRecordMapper.map(mock(RecordsMetadata.class), variantContext, samples);

    verify(variantContext).getGenotypesOrderedBy(Arrays.asList("c", "b", "a"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void mapWithIdentifiersQualityFilters() {
    String contig = "MyContig";
    int position = 123;
    String referenceAllele = "C";
    List<String> altAlleles = List.of("C", "T");

    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn(contig);
    when(variantContext.getStart()).thenReturn(position);
    when(variantContext.getReference()).thenReturn(Allele.REF_C);
    when(variantContext.getAlternateAlleles()).thenReturn(List.of(Allele.ALT_C, Allele.ALT_T));

    String idStr = "rs0a;rs0b";
    double quality = 1.23;
    when(variantContext.hasID()).thenReturn(true);
    when(variantContext.hasLog10PError()).thenReturn(true);
    when(variantContext.filtersWereApplied()).thenReturn(true);

    when(variantContext.getID()).thenReturn(idStr);
    when(variantContext.getPhredScaledQual()).thenReturn(quality);
    when(variantContext.getFilters()).thenReturn(Set.of("q10", "s50"));

    RecordSample recordSample = mock(RecordSample.class);
    Genotype genotype = mock(Genotype.class);
    when(variantContext.getGenotypesOrderedBy(List.of("sample0"))).thenReturn(List.of(genotype));
    Map<String, Object> attributes = emptyMap();
    when(variantContext.getAttributes()).thenReturn(attributes);
    Info info = new Info();
    List<InfoMetadata> infoMetadataList = emptyList();
    List<FormatMetadata> formatMetadataList = emptyList();
    RecordsMetadata recordsMetadata = new RecordsMetadata(infoMetadataList, formatMetadataList);
    when(htsJdkToInfoMapper
        .map((List<CompoundMetadata<Info>>) (List<?>) infoMetadataList, attributes))
        .thenReturn(info);
    when(htsJdkToRecordSampleMapper.map(emptyList(), genotype)).thenReturn(recordSample);

    Record record =
        new Record(
            contig,
            position,
            List.of("rs0a", "rs0b"),
            referenceAllele,
            altAlleles,
            quality,
            List.of("q10", "s50"),
            info,
            List.of(recordSample));

    Sample sample0 = Sample.builder().person(new Person(
        "family",
        "sample0",
        MISSING_PERSON_ID,
        MISSING_PERSON_ID,
        Sex.UNKNOWN_SEX,
        AffectedStatus.MISSING)).index(0).build();
    Assertions.assertEquals(
        record, htsJdkToRecordMapper.map(recordsMetadata, variantContext, List.of(sample0)));
  }

  @Test
  void mapMissingContig() {
    VariantContext variantContext = mock(VariantContext.class);
    List<Sample> samples = emptyList();
    RecordsMetadata recordsMetadata = mock(RecordsMetadata.class);
    assertThrows(
        VcfParseException.class,
        () -> htsJdkToRecordMapper.map(recordsMetadata, variantContext, samples));
  }
}
