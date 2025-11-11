package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissingSampleExceptionTest {
    @Test
    void getMessage() {
        assertEquals(
                "No sample found for sampleId 'TEST'",
                new MissingSampleException("TEST").getMessage());
    }
}