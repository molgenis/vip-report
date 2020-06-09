package org.molgenis.vcf.report.mapper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.report.model.Info;

class HtsJdkToInfoMapperTest {
  private HtsJdkToInfoMapper htsJdkToInfoMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToInfoMapper = new HtsJdkToInfoMapper();
  }

  @Test
  void mapNoInfoHeaderLine() {
    String key = "attr";
    Object value = "value";
    VCFHeader vcfHeader = mock(VCFHeader.class);
    Info info = new Info();
    info.put(key, value);
    assertEquals(info, htsJdkToInfoMapper.map(vcfHeader, singletonMap(key, value)));
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(VCFHeaderLineType vcfHeaderLineType, Object value, int count, Object expectedValue) {
    String key = "attr";
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfInfoHeaderLine.getType()).thenReturn(vcfHeaderLineType);
    when(vcfInfoHeaderLine.getCountType()).thenReturn(VCFHeaderLineCount.INTEGER);
    when(vcfInfoHeaderLine.getCount()).thenReturn(count);
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.hasInfoLine(key)).thenReturn(true);
    when(vcfHeader.getInfoHeaderLine(key)).thenReturn(vcfInfoHeaderLine);
    Info info = new Info();
    info.put(key, expectedValue);
    assertEquals(info, htsJdkToInfoMapper.map(vcfHeader, singletonMap(key, value)));
  }

  private static Stream<Arguments> map() {
    return Stream.of(
        Arguments.of(VCFHeaderLineType.Integer, 1, 1, 1),
        Arguments.of(VCFHeaderLineType.Integer, "1", 1, 1),
        Arguments.of(VCFHeaderLineType.Integer, asList(1, 2), 2, asList(1, 2)),
        Arguments.of(VCFHeaderLineType.Integer, "1,2", 2, asList(1, 2)),
        Arguments.of(VCFHeaderLineType.Integer, asList(".", null), 2, asList(null, null)),
        Arguments.of(VCFHeaderLineType.Float, 1.23, 1, 1.23),
        Arguments.of(VCFHeaderLineType.Float, "1.23", 1, 1.23),
        Arguments.of(VCFHeaderLineType.Float, asList(1.23, 2.34), 2, asList(1.23, 2.34)),
        Arguments.of(VCFHeaderLineType.Float, "1.23,2.34", 2, asList(1.23, 2.34)),
        Arguments.of(VCFHeaderLineType.Float, asList(".", null), 2, asList(null, null)),
        Arguments.of(VCFHeaderLineType.String, "str", 1, "str"),
        Arguments.of(VCFHeaderLineType.String, asList("str1", "str2"), 2, asList("str1", "str2")),
        Arguments.of(VCFHeaderLineType.String, "str1,str2", 2, asList("str1", "str2")),
        Arguments.of(VCFHeaderLineType.String, asList(".", null), 2, asList(null, null)),
        Arguments.of(VCFHeaderLineType.String, ".,.", 2, asList(null, null)),
        Arguments.of(VCFHeaderLineType.Flag, true, 0, true),
        Arguments.of(VCFHeaderLineType.Flag, false, 0, false),
        Arguments.of(VCFHeaderLineType.Flag, "true", 0, true),
        Arguments.of(VCFHeaderLineType.Flag, "false", 0, false),
        Arguments.of(VCFHeaderLineType.Flag, null, 0, false));
  }
}
