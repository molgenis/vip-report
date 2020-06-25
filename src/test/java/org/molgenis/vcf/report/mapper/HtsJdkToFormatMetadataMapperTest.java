package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

class HtsJdkToFormatMetadataMapperTest {

  private HtsJdkToFormatMetadataMapper htsJdkToFormatMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToFormatMetadataMapper = new HtsJdkToFormatMetadataMapper();
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(VCFHeaderLineType vcfHeaderLineType, FormatMetadata.Type type) {
    String id = "MyId";
    String description = "My Description";
    int count = 1;

    VCFFormatHeaderLine vcfFormatHeaderLine = mock(VCFFormatHeaderLine.class);
    when(vcfFormatHeaderLine.getID()).thenReturn(id);
    when(vcfFormatHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.INTEGER);
    when(vcfFormatHeaderLine.getCount()).thenReturn(count);
    when(vcfFormatHeaderLine.getType()).thenReturn(vcfHeaderLineType);
    when(vcfFormatHeaderLine.getDescription()).thenReturn(description);

    FormatMetadata formatMetadata =
        FormatMetadata.builder()
            .id(id)
            .number(Number.builder().type(Type.NUMBER).count(count).build())
            .type(type)
            .description(description)
            .build();
    assertEquals(formatMetadata, htsJdkToFormatMetadataMapper.map(vcfFormatHeaderLine));
  }

  private static Stream<Arguments> map() {
    return Stream.of(
        Arguments.of(VCFHeaderLineType.String, FormatMetadata.Type.STRING),
        Arguments.of(VCFHeaderLineType.Float, FormatMetadata.Type.FLOAT),
        Arguments.of(VCFHeaderLineType.Integer, FormatMetadata.Type.INTEGER),
        Arguments.of(VCFHeaderLineType.Character, FormatMetadata.Type.CHARACTER));
  }
}
