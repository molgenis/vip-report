package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseExceptionTest {
    @Test
    void getMessage() {
        assertEquals(
                "Error while communicating with the database: 'TEST'",
                new DatabaseException("TEST").getMessage());
    }
}