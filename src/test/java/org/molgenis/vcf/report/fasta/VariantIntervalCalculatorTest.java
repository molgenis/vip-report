package org.molgenis.vcf.report.fasta;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.SampleSettings;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariantIntervalCalculatorTest {

    @Mock
    private VcfIntervalCalculator vcfIntervalCalculator;
    @Mock
    private CramIntervalCalculator cramIntervalCalculator;
    private VariantIntervalCalculator variantIntervalCalculator;

    @BeforeEach
    void setUpBeforeEach() {
        variantIntervalCalculator = new VariantIntervalCalculator(vcfIntervalCalculator, cramIntervalCalculator);
    }
    @Test
    void calculate() {
        VCFFileReader vcfFileReader = mock(VCFFileReader.class);
        VCFHeader vcfHeader = mock(VCFHeader.class);
        when(vcfFileReader.getHeader()).thenReturn(vcfHeader);
        SampleSettings.CramPath cramPath = mock(SampleSettings.CramPath.class);
        Map<String, SampleSettings.CramPath> cramPaths = Map.of("TEST", cramPath);
        Path reference = Path.of("fake/reference/path");

        when(vcfIntervalCalculator.calculate(vcfHeader,vcfFileReader,250, null)).thenReturn(Map.of("1", List.of(new ContigInterval("1",1,100),new ContigInterval("1",200,300))));
        when(cramIntervalCalculator.calculate(cramPaths, reference)).thenReturn(Map.of("1", List.of(new ContigInterval("1",50,150),new ContigInterval("1",500,600)), "4", List.of(new ContigInterval("4",50,150))));

        List<ContigInterval> expected = List.of(new ContigInterval("1",1,150),new ContigInterval("1",200,300),new ContigInterval("1",500,600),new ContigInterval("4",50,150));
        assertEquals(expected, variantIntervalCalculator.calculate(vcfFileReader, cramPaths, reference));
    }
}