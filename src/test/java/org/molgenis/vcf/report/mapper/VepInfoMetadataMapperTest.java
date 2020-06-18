package org.molgenis.vcf.report.mapper;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.mapper.info.VepInfoMetadataMapper;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

class VepInfoMetadataMapperTest {
  private VepInfoMetadataMapper vepInfoMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    vepInfoMetadataMapper = new VepInfoMetadataMapper();
  }

  @Test
  void canMap() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Consequence annotations from Ensembl VEP. Format: X|Y");
    assertTrue(vepInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void canMapFalse() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getDescription()).thenReturn("My Description");
    assertFalse(vepInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @Test
  void map() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn("CSQ");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.UNBOUNDED);
    when(vcfInfoHeaderLine.getType()).thenReturn(VCFHeaderLineType.String);
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Consequence annotations from Ensembl VEP. Format: X|Y");

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
            "CSQ",
            new Number(Type.OTHER, null, ','),
            InfoMetadata.Type.NESTED,
            "Consequence annotations from Ensembl VEP. Format: X|Y",
            null,
            null,
            asList(xInfoMetadata, yInfoMetadata));
    assertEquals(infoMetadata, vepInfoMetadataMapper.map(vcfInfoHeaderLine));
  }

  @Test
  void mapCantMap() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getDescription()).thenReturn("My Description");
    assertThrows(
        IllegalArgumentException.class, () -> vepInfoMetadataMapper.map(vcfInfoHeaderLine));
  }

  @Test
  void mapInvalidNumber() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Consequence annotations from Ensembl VEP. Format: X|Y");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.INTEGER);
    assertThrows(
        IllegalArgumentException.class, () -> vepInfoMetadataMapper.map(vcfInfoHeaderLine));
  }

  @Test
  void mapInvalidType() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getDescription())
        .thenReturn("Consequence annotations from Ensembl VEP. Format: X|Y");
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.UNBOUNDED);
    when(vcfInfoHeaderLine.getType()).thenReturn(VCFHeaderLineType.Float);
    assertThrows(
        IllegalArgumentException.class, () -> vepInfoMetadataMapper.map(vcfInfoHeaderLine));
  }
}
