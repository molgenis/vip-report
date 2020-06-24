package org.molgenis.vcf.report.mapper;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata.Type;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;

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
    Info info = new Info();
    info.put(key, value);
    List<CompoundMetadata<Info>> infoMetadataList = emptyList();
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(CompoundMetadata.Type type, Object value, int count, Object expectedValue) {
    String key = "attr";
    Info info = new Info();
    info.put(key, expectedValue);

    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id(key)
            .number(new Number(Number.Type.NUMBER, count, ','))
            .type(type)
            .description("My Description")
            .build();
    List<CompoundMetadata<Info>> infoMetadataList = singletonList(infoMetadata);
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }

  private static Stream<Arguments> map() {
    return Stream.of(
        Arguments.of(Type.INTEGER, 1, 1, 1),
        Arguments.of(Type.INTEGER, "1", 1, 1),
        Arguments.of(Type.INTEGER, asList(1, 2), 2, asList(1, 2)),
        Arguments.of(Type.INTEGER, "1,2", 2, asList(1, 2)),
        Arguments.of(Type.INTEGER, asList(".", null), 2, asList(null, null)),
        Arguments.of(Type.FLOAT, 1.23, 1, 1.23),
        Arguments.of(Type.FLOAT, "1.23", 1, 1.23),
        Arguments.of(Type.FLOAT, asList(1.23, 2.34), 2, asList(1.23, 2.34)),
        Arguments.of(Type.FLOAT, "1.23,2.34", 2, asList(1.23, 2.34)),
        Arguments.of(Type.FLOAT, asList(".", null), 2, asList(null, null)),
        Arguments.of(Type.STRING, "str", 1, "str"),
        Arguments.of(Type.STRING, singletonList("str"), 1, "str"),
        Arguments.of(Type.STRING, asList("str1", "str2"), 2, asList("str1", "str2")),
        Arguments.of(Type.STRING, "str1,str2", 2, asList("str1", "str2")),
        Arguments.of(Type.STRING, asList(".", null), 2, asList(null, null)),
        Arguments.of(Type.STRING, ".,.", 2, asList(null, null)),
        Arguments.of(Type.CHARACTER, "str", 1, "str"),
        Arguments.of(Type.CHARACTER, singletonList("str"), 1, "str"),
        Arguments.of(Type.FLAG, true, 0, true),
        Arguments.of(Type.FLAG, false, 0, false),
        Arguments.of(Type.FLAG, "true", 0, true),
        Arguments.of(Type.FLAG, "false", 0, false),
        Arguments.of(Type.FLAG, null, 0, false));
  }

  @Test
  void mapNested() {
    Object value = "a|0";

    String nestedKey0 = "nestedAttr0";
    InfoMetadata nestedInfoMetadata0 =
        InfoMetadata.builder()
            .id(nestedKey0)
            .number(new Number(Number.Type.NUMBER, 1, ','))
            .type(Type.STRING)
            .description(nestedKey0)
            .build();

    String nestedKey1 = "nestedAttr1";
    InfoMetadata nestedInfoMetadata1 =
        InfoMetadata.builder()
            .id(nestedKey1)
            .number(new Number(Number.Type.NUMBER, 1, ','))
            .type(Type.INTEGER)
            .description(nestedKey1)
            .build();

    String key = "attr";
    Info info = new Info();
    info.put(key, asList("a", 0));

    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id(key)
            .number(new Number(Number.Type.NUMBER, 1, ','))
            .type(Type.NESTED)
            .description("My Description")
            .nestedMetadata(asList(nestedInfoMetadata0, nestedInfoMetadata1))
            .build();
    List<CompoundMetadata<Info>> infoMetadataList = singletonList(infoMetadata);
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }

  @Test
  void mapNestedList() {
    Object value = "a|0,b|1";

    String nestedKey0 = "nestedAttr0";
    InfoMetadata nestedInfoMetadata0 =
        InfoMetadata.builder()
            .id(nestedKey0)
            .number(new Number(Number.Type.NUMBER, 1, ','))
            .type(Type.STRING)
            .description(nestedKey0)
            .build();

    String nestedKey1 = "nestedAttr1";
    InfoMetadata nestedInfoMetadata1 =
        InfoMetadata.builder()
            .id(nestedKey1)
            .number(new Number(Number.Type.NUMBER, 1, ','))
            .type(Type.INTEGER)
            .description(nestedKey1)
            .build();

    String key = "attr";
    Info info = new Info();
    info.put(key, asList(asList("a", 0), asList("b", 1)));

    InfoMetadata infoMetadata =
        InfoMetadata.builder()
            .id(key)
            .number(new Number(Number.Type.OTHER, null, ','))
            .type(Type.NESTED)
            .description("My Description")
            .nestedMetadata(asList(nestedInfoMetadata0, nestedInfoMetadata1))
            .build();
    List<CompoundMetadata<Info>> infoMetadataList = singletonList(infoMetadata);
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }
}
