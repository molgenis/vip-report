package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.model.Bytes;

@ExtendWith(MockitoExtension.class)
class VariantFastaSlicerTest {

    @Mock
    private FastaSlicer fastaSlicer;
    @Mock
    private VariantIntervalCalculator variantIntervalCalculator;
    private VariantFastaSlicer variantFastaSlicer;

    @BeforeEach
    void setUpBeforeEach() {
        variantFastaSlicer = new VariantFastaSlicer(fastaSlicer, variantIntervalCalculator);
    }

    @Test
    void generate() {
        ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
        ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);
        ContigInterval contigInterval2 = new ContigInterval("3", 2750, 3250);
        ContigInterval contigInterval3 = new ContigInterval("4", 3750, 4250);

        VCFHeader vcfHeader = mock(VCFHeader.class);
        VCFFileReader vcfFileReader = mock(VCFFileReader.class);

        SampleSettings.CramPath cramPath = mock(SampleSettings.CramPath.class);
        Map<String, SampleSettings.CramPath> cramPaths = Map.of("TEST", cramPath);
        Path referencePath = Path.of("fake/reference/path");
        when(variantIntervalCalculator.calculate(vcfFileReader, cramPaths, referencePath))
                .thenReturn(List.of(contigInterval0,contigInterval1,contigInterval2,contigInterval3));

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
                variantFastaSlicer.generate(vcfFileReader, cramPaths, referencePath));
    }

    @Test
    void generateWithoutCram() {
        ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
        ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);

        VCFHeader vcfHeader = mock(VCFHeader.class);
        VCFFileReader vcfFileReader = mock(VCFFileReader.class);

        Path referencePath = Path.of("fake/reference/path");
        when(variantIntervalCalculator.calculate(vcfFileReader, null, referencePath))
                .thenReturn(List.of(contigInterval0,contigInterval1));

        FastaSlice fastaSlice0 = new FastaSlice(contigInterval0, "ACTG".getBytes(StandardCharsets.UTF_8));
        FastaSlice fastaSlice1 = new FastaSlice(contigInterval1, "ACTG".getBytes(StandardCharsets.UTF_8));

        doReturn(fastaSlice0).when(fastaSlicer).slice(contigInterval0);
        doReturn(fastaSlice1).when(fastaSlicer).slice(contigInterval1);

        assertEquals(Map.of("1:750-1250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8)), "2:1750-2250", new Bytes("ACTG".getBytes(StandardCharsets.UTF_8))),
                variantFastaSlicer.generate(vcfFileReader, null, Path.of("fake/reference/path")));
    }
}
