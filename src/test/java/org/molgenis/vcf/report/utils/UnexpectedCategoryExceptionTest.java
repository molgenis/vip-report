package org.molgenis.vcf.report.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnexpectedCategoryExceptionTest {
    @Test
    void getMessage() {
        assertEquals(
                "Unexpected value 'VALUE' for categorical field 'TEST'",
                new UnexpectedCategoryException("TEST","VALUE").getMessage());
    }
}