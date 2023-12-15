package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.samtools.CRAMFileReader;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.model.Bytes;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class VcfFastaSlicerTest {

    @Mock
    private FastaSlicer fastaSlicer;
    @Mock
    private VcfIntervalCalculator vcfIntervalCalculator;
    @Mock
    private CramIntervalCalculator cramIntervalCalculator;
    private VcfFastaSlicer vcfFastaSlicer;

    @BeforeEach
    void setUpBeforeEach() {
        vcfFastaSlicer = new VcfFastaSlicer(fastaSlicer, vcfIntervalCalculator, cramIntervalCalculator);
    }

    @Test
    void generate() {
        ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
        ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);
        ContigInterval contigInterval2 = new ContigInterval("3", 2750, 3250);
        ContigInterval contigInterval3 = new ContigInterval("4", 3750, 4250);

        VCFHeader vcfHeader = mock(VCFHeader.class);
        VCFFileReader vcfFileReader = mock(VCFFileReader.class);
        when(vcfFileReader.getHeader()).thenReturn(vcfHeader);

        when(vcfIntervalCalculator.computeIntervalMap(vcfHeader, vcfFileReader,250, null))
                .thenReturn(Map.of("1", List.of(contigInterval0), "2", List.of(contigInterval1)));
        SampleSettings.CramPath cramPath = mock(SampleSettings.CramPath.class);
        Map<String, SampleSettings.CramPath> cramPaths = Map.of("TEST", cramPath);
        Path referencePath = Path.of("fake/reference/path");
        when(cramIntervalCalculator.computeIntervalMap(cramPaths, referencePath))
                .thenReturn(Map.of("3",List.of(contigInterval2), "4", List.of(contigInterval3)));
        FastaSlice fastaSlice0 = new FastaSlice(contigInterval0, "ACTG".getBytes(StandardCharsets.UTF_8));
        FastaSlice fastaSlice1 = new FastaSlice(contigInterval1, "ACTG".getBytes(StandardCharsets.UTF_8));
        FastaSlice fastaSlice2 = new FastaSlice(contigInterval2, "ACTG".getBytes(StandardCharsets.UTF_8));
        FastaSlice fastaSlice3 = new FastaSlice(contigInterval3, "ACTG".getBytes(StandardCharsets.UTF_8));

        doReturn(fastaSlice0).when(fastaSlicer).slice(contigInterval0);
        doReturn(fastaSlice1).when(fastaSlicer).slice(contigInterval1);
        doReturn(fastaSlice2).when(fastaSlicer).slice(contigInterval2);
        doReturn(fastaSlice3).when(fastaSlicer).slice(contigInterval3);

        assertEquals(Map.of(
                "1:750-1250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8)),
                "2:1750-2250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8)),
                "3:2750-3250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8)),
                "4:3750-4250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8))),
                vcfFastaSlicer.generate(vcfFileReader, cramPaths, referencePath));
    }

    @Test
    void generateWithoutCram() {
        ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
        ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);

        VCFHeader vcfHeader = mock(VCFHeader.class);
        VCFFileReader vcfFileReader = mock(VCFFileReader.class);
        when(vcfFileReader.getHeader()).thenReturn(vcfHeader);

        when(vcfIntervalCalculator.calculate(vcfHeader, vcfFileReader, 250))
                .thenReturn(List.of(contigInterval0, contigInterval1));

        FastaSlice fastaSlice0 = new FastaSlice(contigInterval0, "ACTG".getBytes(StandardCharsets.UTF_8));
        FastaSlice fastaSlice1 = new FastaSlice(contigInterval1, "ACTG".getBytes(StandardCharsets.UTF_8));

        doReturn(fastaSlice0).when(fastaSlicer).slice(contigInterval0);
        doReturn(fastaSlice1).when(fastaSlicer).slice(contigInterval1);

        assertEquals(Map.of("1:750-1250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8)), "2:1750-2250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8))),
                vcfFastaSlicer.generate(vcfFileReader, null, Path.of("fake/reference/path")));
    }

    private <T> CloseableIterator<T> createCloseableIterator(Iterator<T> iterator) {
        return new CloseableIterator<T>() {
            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
