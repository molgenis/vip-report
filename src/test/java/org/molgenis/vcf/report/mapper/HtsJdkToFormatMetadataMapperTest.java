package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

class HtsJdkToFormatMetadataMapperTest {

  private HtsJdkToFormatMetadataMapper htsJdkToFormatMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToFormatMetadataMapper = new HtsJdkToFormatMetadataMapper();
  }

  @Test
  void map() {
    String id = "MyId";
    String description = "My Description";
    int count = 1;

    VCFFormatHeaderLine vcfFormatHeaderLine = mock(VCFFormatHeaderLine.class);
    when(vcfFormatHeaderLine.getID()).thenReturn(id);
    when(vcfFormatHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.INTEGER);
    when(vcfFormatHeaderLine.getCount()).thenReturn(count);
    when(vcfFormatHeaderLine.getType()).thenReturn(VCFHeaderLineType.String);
    when(vcfFormatHeaderLine.getDescription()).thenReturn(description);

    FormatMetadata formatMetadata =
        new FormatMetadata(
            id, new Number(Type.NUMBER, count, ','), FormatMetadata.Type.STRING, description);
    assertEquals(formatMetadata, htsJdkToFormatMetadataMapper.map(vcfFormatHeaderLine));
  }
}
