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
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata.Type;
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
    List<InfoMetadata> infoMetadataList = emptyList();
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(Type type, Object value, int count, Object expectedValue) {
    String key = "attr";
    Info info = new Info();
    info.put(key, expectedValue);

    InfoMetadata infoMetadata =
        new InfoMetadata(
            key,
            new Number(Number.Type.NUMBER, count, ','),
            type,
            "My Description",
            null,
            null,
            emptyList());
    List<InfoMetadata> infoMetadataList = singletonList(infoMetadata);
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
        Arguments.of(Type.STRING, asList("str1", "str2"), 2, asList("str1", "str2")),
        Arguments.of(Type.STRING, "str1,str2", 2, asList("str1", "str2")),
        Arguments.of(Type.STRING, asList(".", null), 2, asList(null, null)),
        Arguments.of(Type.STRING, ".,.", 2, asList(null, null)),
        Arguments.of(Type.CHARACTER, "str", 1, "str"),
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
        new InfoMetadata(
            nestedKey0,
            new Number(Number.Type.NUMBER, 1, ','),
            Type.STRING,
            nestedKey0,
            null,
            null,
            emptyList());

    String nestedKey1 = "nestedAttr1";
    InfoMetadata nestedInfoMetadata1 =
        new InfoMetadata(
            nestedKey1,
            new Number(Number.Type.NUMBER, 1, ','),
            Type.INTEGER,
            nestedKey0,
            null,
            null,
            emptyList());

    Info nestedInfo0 = new Info();
    nestedInfo0.put(nestedKey0, "a");
    nestedInfo0.put(nestedKey1, 0);

    String key = "attr";
    Info info = new Info();
    info.put(key, nestedInfo0);

    InfoMetadata infoMetadata =
        new InfoMetadata(
            key,
            new Number(Number.Type.NUMBER, 1, ','),
            Type.NESTED,
            "My Description",
            null,
            null,
            asList(nestedInfoMetadata0, nestedInfoMetadata1));
    List<InfoMetadata> infoMetadataList = singletonList(infoMetadata);
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }

  @Test
  void mapNestedList() {
    Object value = "a|0,b|1";

    String nestedKey0 = "nestedAttr0";
    InfoMetadata nestedInfoMetadata0 =
        new InfoMetadata(
            nestedKey0,
            new Number(Number.Type.NUMBER, 1, ','),
            Type.STRING,
            nestedKey0,
            null,
            null,
            emptyList());

    String nestedKey1 = "nestedAttr1";
    InfoMetadata nestedInfoMetadata1 =
        new InfoMetadata(
            nestedKey1,
            new Number(Number.Type.NUMBER, 1, ','),
            Type.INTEGER,
            nestedKey0,
            null,
            null,
            emptyList());

    Info nestedInfo0 = new Info();
    nestedInfo0.put(nestedKey0, "a");
    nestedInfo0.put(nestedKey1, 0);

    Info nestedInfo1 = new Info();
    nestedInfo1.put(nestedKey0, "b");
    nestedInfo1.put(nestedKey1, 1);

    String key = "attr";
    Info info = new Info();
    info.put(key, asList(nestedInfo0, nestedInfo1));

    InfoMetadata infoMetadata =
        new InfoMetadata(
            key,
            new Number(Number.Type.OTHER, null, ','),
            Type.NESTED,
            "My Description",
            null,
            null,
            asList(nestedInfoMetadata0, nestedInfoMetadata1));
    List<InfoMetadata> infoMetadataList = singletonList(infoMetadata);
    assertEquals(info, htsJdkToInfoMapper.map(infoMetadataList, singletonMap(key, value)));
  }
}
