package org.molgenis.vcf.report.mapper;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Genotype;
import org.molgenis.vcf.report.model.Genotype.Type;
import org.molgenis.vcf.report.model.RecordSample;

@ExtendWith(MockitoExtension.class)
class HtsJdkToRecordSampleMapperTest {

  @Mock private HtsJdkToGenotypeTypeMapper htsJdkGenotypeTypeMapper;
  private HtsJdkToRecordSampleMapper htsJdkToRecordSampleMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToRecordSampleMapper = new HtsJdkToRecordSampleMapper(htsJdkGenotypeTypeMapper);
  }

  @Test
  void map() {
    htsjdk.variant.variantcontext.Genotype htsJdkGenotype =
        mock(htsjdk.variant.variantcontext.Genotype.class);

    when(htsJdkGenotype.hasDP()).thenReturn(true);
    when(htsJdkGenotype.getDP()).thenReturn(10);
    when(htsJdkGenotype.hasAD()).thenReturn(true);
    when(htsJdkGenotype.getAD()).thenReturn(new int[]{1, 2});
    when(htsJdkGenotype.hasGQ()).thenReturn(true);
    when(htsJdkGenotype.getGQ()).thenReturn(5);
    when(htsJdkGenotype.hasPL()).thenReturn(true);
    when(htsJdkGenotype.getPL()).thenReturn(new int[]{3, 4});

    when(htsJdkGenotype.isAvailable()).thenReturn(true);

    GenotypeType htsGenotypeType = GenotypeType.HET;
    when(htsJdkGenotype.getType()).thenReturn(htsGenotypeType);
    when(htsJdkGenotype.isPhased()).thenReturn(true);
    when(htsJdkGenotype.getAlleles()).thenReturn(List.of(Allele.REF_C, Allele.ALT_T));

    when(htsJdkGenotype.getExtendedAttributes()).thenReturn(Collections.singletonMap("XX", "val"));
    when(htsJdkGenotypeTypeMapper.map(htsGenotypeType)).thenReturn(Type.HETEROZYGOUS);

    Genotype genotype = new Genotype(List.of("C", "T"), true, Type.HETEROZYGOUS);
    Map<String, Object> dataMap = new LinkedHashMap<>();
    dataMap.put("GQ", 5);
    dataMap.put("AD", asList(1, 2));
    dataMap.put("DP", 10);
    dataMap.put("PL", asList(3, 4));
    dataMap.put("XX", "val");

    RecordSample recordSample = new RecordSample(genotype, dataMap);
    assertEquals(recordSample, htsJdkToRecordSampleMapper.map(emptyList(), htsJdkGenotype));
  }

  @Test
  void mapUnavailable() {
    htsjdk.variant.variantcontext.Genotype htsJdkGenotype =
        mock(htsjdk.variant.variantcontext.Genotype.class);

    RecordSample recordSample = new RecordSample(null, emptyMap());
    assertEquals(recordSample, htsJdkToRecordSampleMapper.map(emptyList(), htsJdkGenotype));
  }
}
