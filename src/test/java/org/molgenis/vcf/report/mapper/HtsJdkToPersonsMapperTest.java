package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper.MISSING;
import static org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper.MISSING_PERSON_ID;

import htsjdk.variant.vcf.VCFHeader;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.AffectedStatus;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Person;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.Sex;

@ExtendWith(MockitoExtension.class)
class HtsJdkToPersonsMapperTest {

  private HtsJdkToPersonsMapper htsJdkToPersonsMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToPersonsMapper = new HtsJdkToPersonsMapper();
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
    List<Sample> samples =
        List.of(Sample.builder().person(new Person(
                MISSING + "0",
                "sample0",
                MISSING_PERSON_ID,
                MISSING_PERSON_ID,
                Sex.UNKNOWN_SEX,
                AffectedStatus.MISSING)).index(0).build(),
            Sample.builder().person(new Person(
                MISSING + "1",
                "sample1",
                MISSING_PERSON_ID,
                MISSING_PERSON_ID,
                Sex.UNKNOWN_SEX,
                AffectedStatus.MISSING)).index(1).build());
    Items<Sample> expectedSampleItems = new Items<>(samples, 3);
    Assertions.assertEquals(
        expectedSampleItems, htsJdkToPersonsMapper.map(vcfHeader, maxNrSamples));
  }

  @Test
  void mapNoSamples() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.hasGenotypingData()).thenReturn(false);

    Items<Person> expectedSampleItems = new Items<>(emptyList(), 0);
    int maxNrSamples = 2;
    Assertions.assertEquals(
        expectedSampleItems, htsJdkToPersonsMapper.map(vcfHeader, maxNrSamples));
  }
}
