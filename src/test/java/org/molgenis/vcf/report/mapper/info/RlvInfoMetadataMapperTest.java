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
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

class RlvInfoMetadataMapperTest {

  private RlvInfoMetadataMapper rlvInfoMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    rlvInfoMetadataMapper = new RlvInfoMetadataMapper();
  }

  @Test
  void canMap() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("RLV");
    assertTrue(rlvInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalseType() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("CSQ");
    assertFalse(rlvInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void map() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("RLV");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.UNBOUNDED);
    when(vcfInfoHeaderLine.getType()).thenReturn(VCFHeaderLineType.String);
    when(vcfInfoHeaderLine.getDescription()).thenReturn("X | Y");

    InfoMetadata xInfoMetadata =
        InfoMetadata.builder()
            .id("X")
            .number(new Number(Type.NUMBER, 1, ','))
            .type(InfoMetadata.Type.STRING)
            .description("X")
            .build();
    InfoMetadata yInfoMetadata =
        InfoMetadata.builder()
            .id("Y")
            .number(new Number(Type.NUMBER, 1, ','))
            .type(InfoMetadata.Type.STRING)
            .description("Y")
            .build();
    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id("RLV")
            .number(new Number(Type.OTHER, null, ','))
            .type(InfoMetadata.Type.NESTED)
            .description("X | Y")
            .nestedMetadata(asList(xInfoMetadata, yInfoMetadata))
            .build();
    assertEquals(infoMetadata, rlvInfoMetadataMapper.map(vcfInfoHeaderLine));
  }
}
