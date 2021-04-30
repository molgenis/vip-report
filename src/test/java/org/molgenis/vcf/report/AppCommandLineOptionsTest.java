package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_GENES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_RECORDS;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_SAMPLES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PHENOTYPES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_REFERENCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.mapper.IllegalPhenotypeArgumentException;
import org.molgenis.vcf.report.utils.InvalidSamplePhenotypesException;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AppCommandLineOptionsTest {

  @TempDir Path sharedTempDir;

  @Test
  void validateCommandLine() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineNoTemplateNoPed() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineOutputExistsForce() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = ResourceUtils.getFile("classpath:example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineOutputExistsNoForce() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String outputFile = ResourceUtils.getFile("classpath:example.vcf.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals("Output file '" + outputFile + "' already exists", exception.getMessage());
  }

  @Test
  void validateCommandLineOuputDir() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String outputFile = sharedTempDir.toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals("Output file '" + outputFile + "' is not a .html file.", exception.getMessage());
  }

  @Test
  void validateCommandLineInputNotExists() {
    String inputFile = "notexists.vcf";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));

    assertEquals("Input file 'notexists.vcf' does not exist.", exception.getMessage());
  }

  @Test
  void validateCommandLineInputDir() throws FileNotFoundException {
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(sharedTempDir.toString()).when(cmd).getOptionValue(OPT_INPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals(
        "Input file '" + sharedTempDir.toString() + "' is a directory.", exception.getMessage());
  }

  @Test
  void validateCommandLineInputNotVcf() throws FileNotFoundException {
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_INPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals(
        "Input file '" + templateFile + "' is not a .vcf or .vcf.gz file.", exception.getMessage());
  }

  @Test
  void validateCommandLinePedNotExists() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        "notexists.ped" + "," + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));

    assertEquals("Ped file 'notexists.ped' does not exist.", exception.getMessage());
  }

  @Test
  void validateCommandLinePedDir() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        sharedTempDir.toString() + "," + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals(
        "Ped file '" + sharedTempDir.toString() + "' is a directory.", exception.getMessage());
  }

  @Test
  void validateCommandLinePedNoPed() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.vcf").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals("Ped file '" + inputFile + "' is not a .ped file.", exception.getMessage());
  }

  @Test
  void validateCommandLineTemplateNotExists() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineTemplateDir() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = sharedTempDir.toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals(
        "Template file '" + sharedTempDir.toString() + "' is a directory.", exception.getMessage());
  }

  @Test
  void validateCommandLineTemplateNoHtml() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
    assertEquals("Template file '" + inputFile + "' is not a .html file.", exception.getMessage());
  }

  @Test
  void validateCommandLinePheno() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "HP:123456,HP:23456";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLinePhenoSample() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "sample1/HP:123456;HP:234567,sample2/HP:23456";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLinePhenoSampleInvalid() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "sample1/HP:123456/HP:234567";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);

    assertThrows(
        InvalidSamplePhenotypesException.class,
        () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLinePhenoNoCurie() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "HP123456";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);

    assertThrows(
        IllegalPhenotypeArgumentException.class,
        () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLinePhenoMixed() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "sample/HP:123456,HP:234567";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);

    assertThrows(
        MixedPhenotypesException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLineMaxSamples() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "sample/HP:123456,HP:234567";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("10").when(cmd).getOptionValue(OPT_MAX_SAMPLES);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineMaxSamplesNoInt() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("test").when(cmd).getOptionValue(OPT_MAX_SAMPLES);

    assertThrows(
        InvalidIntegerException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLineMaxRecordsNoInt() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pheno = "sample/HP:123456,HP:234567";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("10").when(cmd).getOptionValue(OPT_MAX_RECORDS);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineMaxRecords() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("test").when(cmd).getOptionValue(OPT_MAX_RECORDS);

    assertThrows(
        InvalidIntegerException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateGenes() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String genesFile = ResourceUtils.getFile("classpath:exampleGene.txt.gz").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(true).when(cmd).hasOption(OPT_GENES);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(genesFile).when(cmd).getOptionValue(OPT_GENES);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateReference() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example.fasta.gz").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateReferenceNotExists() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String referenceFile = "invalid.fasta.gz";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateReferenceInvalidFileType() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String referenceFile = "invalid.fasta";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateReferenceNoIndex() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example_no_index.fasta.gz").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_MAX_RECORDS);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_TEMPLATE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }
}
