package org.molgenis.vcf.report.mapper.info;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
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
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Functional annotations: 'X | Y'");
    assertTrue(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalseType() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("CSQ");
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("My Description");
    assertFalse(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalseDescription() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("ANN");
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("My Description");
    assertFalse(snpEffInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void map() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("ANN");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.UNBOUNDED);
    when(vcfInfoHeaderLine.getType()).thenReturn(VCFHeaderLineType.String);
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Functional annotations: 'X | Y'");

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
            "ANN",
            new Number(Type.OTHER, null, ','),
            InfoMetadata.Type.NESTED,
            "Functional annotations: 'X | Y'",
            null,
            null,
            asList(xInfoMetadata, yInfoMetadata));
    assertEquals(infoMetadata, snpEffInfoMetadataMapper.map(vcfInfoHeaderLine));
  }
}