package org.molgenis.vcf.report.mapper.info;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
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
        .thenReturn("Consequence annotations from Ensembl VEP. Format: X|Y|Consequence|PUBMED|HPO");

    InfoMetadata xInfoMetadata =
        InfoMetadata.builder()
            .id("X")
            .number(Number.builder().type(Type.NUMBER).count(1).build())
            .type(CompoundMetadata.Type.STRING)
            .description("X")
            .build();
    InfoMetadata yInfoMetadata =
        InfoMetadata.builder()
            .id("Y")
            .number(Number.builder().type(Type.NUMBER).count(1).build())
            .type(CompoundMetadata.Type.STRING)
            .description("Y")
            .build();
    InfoMetadata consequenceMetadata = InfoMetadata.builder()
        .id("Consequence")
        .number(Number.builder().type(Type.OTHER).separator('&').build())
        .type(CompoundMetadata.Type.STRING)
        .description("Consequence")
        .build();
    InfoMetadata pubmedMetadata = InfoMetadata.builder()
        .id("PUBMED")
        .number(Number.builder().type(Type.OTHER).separator('&').build())
        .type(CompoundMetadata.Type.INTEGER)
        .description("PUBMED")
        .build();
    InfoMetadata hpoMetadata = InfoMetadata.builder()
        .id("HPO")
        .number(Number.builder().type(Type.OTHER).separator('&').build())
        .type(CompoundMetadata.Type.STRING)
        .description("HPO")
        .build();
    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id("CSQ")
            .number(Number.builder().type(Number.Type.OTHER).separator(',').build())
            .type(CompoundMetadata.Type.NESTED)
            .description(
                "Consequence annotations from Ensembl VEP. Format: X|Y|Consequence|PUBMED|HPO")
            .nestedMetadata(
                asList(xInfoMetadata, yInfoMetadata, consequenceMetadata, pubmedMetadata,
                    hpoMetadata))
            .build();
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
