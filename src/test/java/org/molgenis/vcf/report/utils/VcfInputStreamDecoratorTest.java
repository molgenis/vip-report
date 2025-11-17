package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class VcfInputStreamDecoratorTest {

  @Test
  void testPreprocessVCFWithValidInput() throws IOException {
    // Given a VCF file with a CNV:TR allele in the ALT field
    String vcfContent = """
        ##fileformat=VCFv4.2
        #CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO
        1\t123456\t.\tA\t<CNV:TR>,<CNV:TR>\t.\tPASS\t.
        """;

    File mockFile = tmpFileFromString(vcfContent);
    InputStream resultStream = VcfInputStreamDecorator.preprocessVCF(mockFile);

    String result = new BufferedReader(new InputStreamReader(resultStream, StandardCharsets.UTF_8))
        .lines().collect(Collectors.joining("\n"));

    String expectedResult = """
        ##fileformat=VCFv4.2
        #CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO
        1\t123456\t.\tA\t<CNV:TR1>,<CNV:TR2>\t.\tPASS\t.
        """;

    assertEquals(expectedResult.trim(), result.trim());
  }

  @Test
  void testPreprocessVCFWithNoCNVAltField() throws IOException {
    String vcfContent = """
        ##fileformat=VCFv4.2
        #CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO
        1\t123456\t.\tA\tT\t.\tPASS\t.
        1\t123456\t.\tA\tC,T\t.\tPASS\t.
        
        """;

    File mockFile = tmpFileFromString(vcfContent);

    InputStream resultStream = VcfInputStreamDecorator.preprocessVCF(mockFile);

    String result = new BufferedReader(new InputStreamReader(resultStream, StandardCharsets.UTF_8))
        .lines().collect(Collectors.joining("\n"));

    assertEquals(vcfContent.trim(), result.trim());
  }

  @Test
  void testPreprocessVCFWithInvalidLine() throws IOException {
    String invalidContent = """
        ##fileformat=VCFv4.2
        #CHROM\tPOS\tID\tREF
        1\t123456\t.
        """;

    File mockFile = tmpFileFromString(invalidContent);

    assertThrows(InvalidVcfLineException.class,
        () -> VcfInputStreamDecorator.preprocessVCF(mockFile));
  }

  private File tmpFileFromString(String content) throws IOException {
    File tempFile = File.createTempFile("test-", ".vcf");
    Files.writeString(tempFile.toPath(), content);
    return tempFile;
  }
}
