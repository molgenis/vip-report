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
import java.util.Collections;
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
        new InfoMetadata(
            "X",
            new Number(Type.NUMBER, 1, ','),
            InfoMetadata.Type.STRING,
            "X",
            null,
            null,
            Collections.emptyList());
    InfoMetadata yInfoMetadata =
        new InfoMetadata(
            "Y",
            new Number(Type.NUMBER, 1, ','),
            InfoMetadata.Type.STRING,
            "Y",
            null,
            null,
            Collections.emptyList());
    InfoMetadata infoMetadata =
        new InfoMetadata(
            "RLV",
            new Number(Type.OTHER, null, ','),
            InfoMetadata.Type.NESTED,
            "X | Y",
            null,
            null,
            asList(xInfoMetadata, yInfoMetadata));
    assertEquals(infoMetadata, rlvInfoMetadataMapper.map(vcfInfoHeaderLine));
  }
}
