package org.molgenis.vcf.report.fasta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CramFastaSlicerTest {

    @Mock
    private FastaSlicer fastaSlicer;
    @Mock
    private CramIntervalCalculator cramIntervalCalculator;
    private CramFastaSlicer cramFastaSlicer;

    @BeforeEach
    void setUpBeforeEach() {
        cramFastaSlicer = new CramFastaSlicer(fastaSlicer, cramIntervalCalculator);
    }

    @Test
    void generate() throws FileNotFoundException {

        ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
        ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);

        File cramFile = ResourceUtils.getFile("classpath:example.cram");
        File craiFile = ResourceUtils.getFile("classpath:example.cram.crai");

        when(cramIntervalCalculator.calculate(Map.of("TEST", new SampleSettings.CramPath(cramFile.toPath(), craiFile.toPath())), Path.of("fake/reference/path")))
                .thenReturn(List.of(contigInterval0, contigInterval1));

        FastaSlice fastaSlice0 = mock(FastaSlice.class);
        FastaSlice fastaSlice1 = mock(FastaSlice.class);

        doReturn(fastaSlice0).when(fastaSlicer).slice(contigInterval0);
        doReturn(fastaSlice1).when(fastaSlicer).slice(contigInterval1);

        assertEquals(List.of(fastaSlice0, fastaSlice1),
                cramFastaSlicer.generate(Map.of("TEST", new SampleSettings.CramPath(cramFile.toPath(), craiFile.toPath())), Path.of("fake/reference/path")));
    }
}