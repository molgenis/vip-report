package org.molgenis.vcf.report.fasta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CramFastaSlicerFactoryTest {

    @Mock
    private FastaSlicerFactory fastaSlicerFactory;
    @Mock
    private CramIntervalCalculator cramIntervalCalculator;
    private CramFastaSlicerFactory cramFastaSlicerFactory;

    @BeforeEach
    void setUp() {
        cramFastaSlicerFactory = new CramFastaSlicerFactory(fastaSlicerFactory, cramIntervalCalculator);
    }

    @Test
    void create() {
        Path fastaGzPath = Path.of("src", "test", "resources", "example.fasta.gz");
        FastaSlicer fastaSlicer = mock(FastaSlicer.class);
        when(fastaSlicerFactory.create(fastaGzPath)).thenReturn(fastaSlicer);
        assertAll(
                () -> assertNotNull(cramFastaSlicerFactory.create(fastaGzPath)),
                () -> verify(fastaSlicerFactory).create(fastaGzPath));
    }
}