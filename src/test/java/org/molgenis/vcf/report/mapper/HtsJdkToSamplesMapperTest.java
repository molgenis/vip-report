package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeader;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Sample;

class HtsJdkToSamplesMapperTest {

  private HtsJdkToSamplesMapper htsJdkToSamplesMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToSamplesMapper = new HtsJdkToSamplesMapper();
  }

  @Test
  void map() {
    HashMap<String, Integer> sampleNameToOffsetMap = new HashMap<>();
    sampleNameToOffsetMap.put("sample0", 0);
    sampleNameToOffsetMap.put("sample1", 1);
    sampleNameToOffsetMap.put("sample2", 2);

    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.hasGenotypingData()).thenReturn(true);
    when(vcfHeader.getSampleNameToOffset()).thenReturn(sampleNameToOffsetMap);

    int maxNrSamples = 2;
    List<Sample> samples = List.of(new Sample("sample0"), new Sample("sample1"));
    Items<Sample> expectedSampleItems = new Items<>(samples, 3);
    Assertions
        .assertEquals(expectedSampleItems, htsJdkToSamplesMapper.map(vcfHeader, maxNrSamples));
  }

  @Test
  void mapNoSamples() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.hasGenotypingData()).thenReturn(false);

    Items<Sample> expectedSampleItems = new Items<>(emptyList(), 0);
    int maxNrSamples = 2;
    Assertions
        .assertEquals(expectedSampleItems, htsJdkToSamplesMapper.map(vcfHeader, maxNrSamples));
  }
}