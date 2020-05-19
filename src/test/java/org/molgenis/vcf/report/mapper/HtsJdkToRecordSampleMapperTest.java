package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeType;
import java.util.List;
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

    GenotypeType htsGenotypeType = GenotypeType.HET;
    when(htsJdkGenotype.getType()).thenReturn(htsGenotypeType);
    when(htsJdkGenotype.isPhased()).thenReturn(true);
    when(htsJdkGenotype.getAlleles()).thenReturn(List.of(Allele.REF_C, Allele.ALT_T));

    when(htsJdkGenotypeTypeMapper.map(htsGenotypeType)).thenReturn(Type.HETEROZYGOUS);

    Genotype genotype = new Genotype(List.of("C", "T"), true, Type.HETEROZYGOUS);
    RecordSample recordSample = new RecordSample(genotype);
    assertEquals(recordSample, htsJdkToRecordSampleMapper.map(htsJdkGenotype));
  }
}
