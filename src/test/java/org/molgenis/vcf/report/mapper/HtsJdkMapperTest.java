package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Sample;

@ExtendWith(MockitoExtension.class)
class HtsJdkMapperTest {

  @Mock
  private HtsJdkToRecordsMapper htsJdkToRecordsMapper;
  @Mock
  private HtsJdkToSamplesMapper htsJdkToSamplesMapper;
  private HtsJdkMapper htsJdkMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkMapper = new HtsJdkMapper(htsJdkToRecordsMapper, htsJdkToSamplesMapper);
  }

  @Test
  void mapSamples() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    int maxSamples = 123;
    Items<Sample> sampleItems = new Items<>(Collections.emptyList(), maxSamples);
    when(htsJdkToSamplesMapper.map(vcfHeader, maxSamples)).thenReturn(sampleItems);
    assertEquals(sampleItems, htsJdkMapper.mapSamples(vcfHeader, maxSamples));
  }

  @Test
  void mapRecords() {
    Iterable<VariantContext> variantContextIterable = singletonList(mock(VariantContext.class));
    int maxRecords = 123;
    List<Sample> samples = emptyList();
    Items<Record> recordItems = new Items<>(Collections.emptyList(), maxRecords);
    when(htsJdkToRecordsMapper.map(variantContextIterable, maxRecords, samples))
        .thenReturn(recordItems);
    assertEquals(recordItems, htsJdkMapper.mapRecords(variantContextIterable, maxRecords, samples));
  }
}