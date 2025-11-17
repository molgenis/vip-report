package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonExceptionTest {
    @Test
    void getMessage() {
        assertEquals(
                "Error converting data to json: 'TEST'",
                new JsonException("TEST").getMessage());
    }
}