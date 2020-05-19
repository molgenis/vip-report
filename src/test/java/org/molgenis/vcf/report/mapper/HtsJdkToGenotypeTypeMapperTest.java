package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import htsjdk.variant.variantcontext.GenotypeType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.Genotype;
import org.molgenis.vcf.report.model.Genotype.Type;

class HtsJdkToGenotypeTypeMapperTest {

  private HtsJdkToGenotypeTypeMapper htsJdkToGenotypeTypeMapper;

  private static Stream<Arguments> map() {
    return Stream.of(
        Arguments.of(GenotypeType.NO_CALL, Type.NO_CALL),
        Arguments.of(GenotypeType.HOM_REF, Type.HOMOZYGOUS_REF),
        Arguments.of(GenotypeType.HET, Type.HETEROZYGOUS),
        Arguments.of(GenotypeType.HOM_VAR, Type.HOMOZYGOUS_ALT),
        Arguments.of(GenotypeType.MIXED, Type.PARTIAL_CALL));
  }

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToGenotypeTypeMapper = new HtsJdkToGenotypeTypeMapper();
  }

  @ParameterizedTest
  @MethodSource("map")
  void map(GenotypeType htsJdkGenotypeType, Genotype.Type expectedGenotypeType) {
    assertEquals(expectedGenotypeType, htsJdkToGenotypeTypeMapper.map(htsJdkGenotypeType));
  }

  @Test
  void mapUnexpectedEnumException() {
    assertThrows(
        UnexpectedEnumException.class,
        () -> htsJdkToGenotypeTypeMapper.map(GenotypeType.UNAVAILABLE));
  }
}
