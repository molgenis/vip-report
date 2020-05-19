package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.RecordSample;
import org.molgenis.vcf.report.model.Sample;

@ExtendWith(MockitoExtension.class)
class HtsJdkToRecordMapperTest {

  @Mock
  private HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper;
  private HtsJdkToRecordMapper htsJdkToRecordMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToRecordMapper = new HtsJdkToRecordMapper(htsJdkToRecordSampleMapper);
  }

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

    Record record = new Record(contig, position, referenceAllele, altAlleles);
    Assertions.assertEquals(record, htsJdkToRecordMapper.map(variantContext, emptyList()));
  }

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
    when(htsJdkToRecordSampleMapper.map(genotype)).thenReturn(recordSample);

    Record record = new Record(contig, position, referenceAllele, altAlleles);
    record.setIdentifiers(List.of("rs0a", "rs0b"));
    record.setQuality(quality);
    record.setFilterStatus(List.of("q10", "s50"));
    record.setRecordSamples(List.of(recordSample));

    Sample sample0 = new Sample("sample0");
    Assertions.assertEquals(record, htsJdkToRecordMapper.map(variantContext, List.of(sample0)));
  }

  @Test
  void mapMissingContig() {
    assertThrows(VcfParseException.class,
        () -> htsJdkToRecordMapper.map(mock(VariantContext.class), emptyList()));
  }
}