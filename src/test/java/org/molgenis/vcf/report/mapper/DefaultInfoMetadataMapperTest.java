package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.mapper.info.DefaultInfoMetadataMapper;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata.Type;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;

@ExtendWith(MockitoExtension.class)
class DefaultInfoMetadataMapperTest {

  private DefaultInfoMetadataMapper defaultInfoMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    defaultInfoMetadataMapper = new DefaultInfoMetadataMapper();
  }

  @Test
  void canMap() {
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    assertTrue(defaultInfoMetadataMapper.canMap(vcfInfoHeaderLine));
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(
      VCFHeaderLineType vcfHeaderLineType, VCFHeaderLineCount countType, Type type, Number number) {
    String id = "MyId";
    String description = "My Description";
    String source = "MySource";
    String version = "MyVersion";
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getID()).thenReturn(id);
    when(vcfInfoHeaderLine.getType()).thenReturn(vcfHeaderLineType);
    if (vcfHeaderLineType != VCFHeaderLineType.Flag) {
      when(vcfInfoHeaderLine.getCountType()).thenReturn(countType);
      if (countType == VCFHeaderLineCount.INTEGER) {
        when(vcfInfoHeaderLine.getCount()).thenReturn(number.getCount());
      }
    }
    when(vcfInfoHeaderLine.getDescription()).thenReturn(description);
    when(vcfInfoHeaderLine.getSource()).thenReturn(source);
    when(vcfInfoHeaderLine.getVersion()).thenReturn(version);

    assertEquals(
        InfoMetadata.builder()
            .id(id)
            .number(number)
            .type(type)
            .description(description)
            .source(source)
            .version(version)
            .build(),
        defaultInfoMetadataMapper.map(vcfInfoHeaderLine));
  }

  private static Stream<Arguments> map() {
    return Stream.of(
        Arguments.of(VCFHeaderLineType.Flag, VCFHeaderLineCount.INTEGER, Type.FLAG, null),
        Arguments.of(
            VCFHeaderLineType.String,
            VCFHeaderLineCount.UNBOUNDED,
            Type.STRING,
            new Number(Number.Type.OTHER, null, ',')),
        Arguments.of(
            VCFHeaderLineType.Float,
            VCFHeaderLineCount.A,
            Type.FLOAT,
            new Number(Number.Type.PER_ALT, null, ',')),
        Arguments.of(
            VCFHeaderLineType.Integer,
            VCFHeaderLineCount.R,
            Type.INTEGER,
            new Number(Number.Type.PER_ALT_AND_REF, null, ',')),
        Arguments.of(
            VCFHeaderLineType.Character,
            VCFHeaderLineCount.G,
            Type.CHARACTER,
            new Number(Number.Type.PER_GENOTYPE, null, ',')),
        Arguments.of(
            VCFHeaderLineType.Character,
            VCFHeaderLineCount.INTEGER,
            Type.CHARACTER,
            new Number(Number.Type.NUMBER, 2, ',')));
  }
}
