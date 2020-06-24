package org.molgenis.vcf.report.mapper.info;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

class SnpEffInfoMetadataMapperTest {

  private SnpEffInfoMetadataMapper snpEffInfoMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    snpEffInfoMetadataMapper = new SnpEffInfoMetadataMapper();
  }

  @Test
  void canMap() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("ANN");
    when(vcfInfoHeaderLine.getDescription()).thenReturn("Functional annotations: 'X | Y'");
    assertTrue(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalseType() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("CSQ");
    when(vcfInfoHeaderLine.getDescription()).thenReturn("My Description");
    assertFalse(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalseDescription() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("ANN");
    when(vcfInfoHeaderLine.getDescription()).thenReturn("My Description");
    assertFalse(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void map() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("ANN");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.UNBOUNDED);
    when(vcfInfoHeaderLine.getType()).thenReturn(VCFHeaderLineType.String);
    when(vcfInfoHeaderLine.getDescription()).thenReturn("Functional annotations: 'X | Y'");

    InfoMetadata xInfoMetadata =
        InfoMetadata.builder()
            .id("X")
            .number(new Number(Type.NUMBER, 1, ','))
            .type(CompoundMetadata.Type.STRING)
            .description("X")
            .build();
    InfoMetadata yInfoMetadata =
        InfoMetadata.builder()
            .id("Y")
            .number(new Number(Type.NUMBER, 1, ','))
            .type(CompoundMetadata.Type.STRING)
            .description("Y")
            .build();
    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id("ANN")
            .number(new Number(Type.OTHER, null, ','))
            .type(CompoundMetadata.Type.NESTED)
            .description("Functional annotations: 'X | Y'")
            .nestedMetadata(asList(xInfoMetadata, yInfoMetadata))
            .build();
    assertEquals(infoMetadata, snpEffInfoMetadataMapper.map(vcfInfoHeaderLine));
  }
}
