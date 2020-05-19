package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnexpectedEnumExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Unexpected enum constant 'UNEXPECTED_CONSTANT' for type 'MyEnum'",
        new UnexpectedEnumException(MyEnum.UNEXPECTED_CONSTANT).getMessage());
  }

  private enum MyEnum {
    UNEXPECTED_CONSTANT
  }
}
