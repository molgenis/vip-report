package org.molgenis.vcf.report.fasta;

import htsjdk.samtools.CRAMFileReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CramIntervalCalculatorTest {

    private CramIntervalCalculator cramIntervalCalculator;

    @Mock
    CramReaderFactory cramReaderFactory;
    @Mock
    CRAMFileReader cramFileReader;
    @Mock
    SAMRecordIterator samRecordIterator;

    @BeforeEach
    void setUp() {
    }

    @BeforeEach
    void setUpBeforeEach() {
        cramIntervalCalculator = new CramIntervalCalculator(cramReaderFactory);
    }

    @Test
    void calculate() throws FileNotFoundException {
        File cramFile = ResourceUtils.getFile("classpath:example.cram");
        File craiFile = ResourceUtils.getFile("classpath:example.cram.crai");

        SampleSettings.CramPath cramPath = new SampleSettings.CramPath(cramFile.toPath(), craiFile.toPath());
        Path reference = Path.of("fake/reference/path");

        SAMRecord samRecord1 = mock(SAMRecord.class);
        when(samRecord1.getStart()).thenReturn(123);
        when(samRecord1.getEnd()).thenReturn(1234);
        when(samRecord1.getContig()).thenReturn("chr2");
        SAMRecord samRecord2 = mock(SAMRecord.class);
        when(samRecord2.getStart()).thenReturn(999);
        when(samRecord2.getEnd()).thenReturn(19999);
        when(samRecord2.getContig()).thenReturn("chrX");

        when(cramReaderFactory.create(cramPath, reference)).thenReturn(cramFileReader);
        when(cramFileReader.getIterator()).thenReturn(samRecordIterator);
        when(samRecordIterator.stream()).thenReturn(List.of(samRecord1, samRecord2).stream());

        assertEquals(
                Map.of("chr2", List.of(new ContigInterval("chr2", 123, 1234)), "chrX", List.of(new ContigInterval("chrX", 999, 19999))),
                cramIntervalCalculator.calculate(Map.of("TEST", cramPath), reference));
    }
}