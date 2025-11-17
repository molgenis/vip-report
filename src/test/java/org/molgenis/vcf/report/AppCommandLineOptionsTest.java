package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_CRAM;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_GENES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_SAMPLES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PHENOTYPES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_REFERENCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE_CONFIG;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TREE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.utils.InvalidSampleCramException;
import org.molgenis.vcf.utils.InvalidSamplePhenotypesException;
import org.molgenis.vcf.utils.MixedPhenotypesException;
import org.molgenis.vcf.utils.sample.mapper.IllegalPhenotypeArgumentException;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AppCommandLineOptionsTest {

  @TempDir Path sharedTempDir;

  @Test
  void validateCommandLine() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped")
            + ","
            + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineNoTemplateNoPed() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineOutputExistsForce() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped")
            + ","
            + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = ResourceUtils.getFile("classpath:example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

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
  void validateCommandLineOutputDir() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
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
  void validateCommandLineInputDir() {
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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pedFiles = "notexists.ped" + "," + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pedFiles =
        sharedTempDir.toString() + "," + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:exampleCmdline.vcf")
            + ","
            + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped")
            + ","
            + ResourceUtils.getFile("classpath:example2.ped");
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pedFiles).when(cmd).getOptionValue(OPT_PED);
    doReturn(outputFile).when(cmd).getOptionValue(OPT_OUTPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineTemplateDir() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();
    String templateFile = sharedTempDir.toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);

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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String outputFile = sharedTempDir.resolve("exampleCmdline.vcf.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(true).when(cmd).hasOption(OPT_OUTPUT);
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
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pheno = "HP:123456,HP:23456";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLinePhenoSample() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pheno = "sample1/HP:123456;HP:234567,sample2/HP:23456";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLinePhenoSampleInvalid() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pheno = "sample1/HP:123456/HP:234567";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);

    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        InvalidSamplePhenotypesException.class,
        () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLinePhenoNoCurie() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pheno = "HP123456";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        IllegalPhenotypeArgumentException.class,
        () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLinePhenoMixed() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String pheno = "sample/HP:123456,HP:234567";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(pheno).when(cmd).getOptionValue(OPT_PHENOTYPES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        MixedPhenotypesException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateCommandLineMaxSamples() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("10").when(cmd).getOptionValue(OPT_MAX_SAMPLES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateCommandLineMaxSamplesNoInt() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(true).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn("test").when(cmd).getOptionValue(OPT_MAX_SAMPLES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        InvalidIntegerException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateGenes() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String genesFile = ResourceUtils.getFile("classpath:example.genes.gff.gz").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(true).when(cmd).hasOption(OPT_GENES);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(genesFile).when(cmd).getOptionValue(OPT_GENES);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateReference() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example.fasta.gz").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    AppCommandLineOptions.validateCommandLine(cmd);
  }

  @Test
  void validateReferenceNotExists() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String referenceFile = "invalid.fasta.gz";
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateReferenceInvalidFileType() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateReferenceNoIndex() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example_no_index.fasta.gz").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  private CommandLine validateBamInit(String bamPathString) throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();
    String referenceFile = ResourceUtils.getFile("classpath:example.fasta.gz").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(true).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(false).when(cmd).hasOption(OPT_GENES);
    doReturn(true).when(cmd).hasOption(OPT_CRAM);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(bamPathString).when(cmd).getOptionValue(OPT_CRAM);
    doReturn(referenceFile).when(cmd).getOptionValue(OPT_REFERENCE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    return cmd;
  }

  private CommandLine validateBamInitMissingReference(String bamPathString)
      throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(false).when(cmd).hasOption(OPT_GENES);
    doReturn(true).when(cmd).hasOption(OPT_CRAM);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    return cmd;
  }

  @Test
  void validateBamInvalidValue() throws FileNotFoundException {
    CommandLine cmd = validateBamInit("my.bam");
    assertThrows(
        InvalidSampleCramException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateBamInvalidValueMissingReference() throws FileNotFoundException {
    CommandLine cmd = validateBamInitMissingReference("my.bam");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateBamInvalidFileTypeValue() throws FileNotFoundException {
    CommandLine cmd = validateBamInit("sample0=invalid.cram");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateBamInvalidFileTypeValueNoIndex() throws FileNotFoundException {
    CommandLine cmd = validateBamInit("sample0=example_no_index.cram");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateBamNotExists() throws FileNotFoundException {
    CommandLine cmd = validateBamInit("sample0=invalid.bam");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  private CommandLine validateTreeInit(String treePathString) throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(false).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(false).when(cmd).hasOption(OPT_GENES);
    doReturn(false).when(cmd).hasOption(OPT_CRAM);
    doReturn(true).when(cmd).hasOption(OPT_TREE);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);
    doReturn(treePathString).when(cmd).getOptionValue(OPT_TREE);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    return cmd;
  }

  @Test
  void validateTreeValidValue() throws FileNotFoundException {
    String treePath = ResourceUtils.getFile("classpath:tree.json").toString();
    CommandLine cmd = validateTreeInit(treePath);
    assertDoesNotThrow(() -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateTreeInvalidFileTypeValue() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    CommandLine cmd = validateTreeInit(inputFile);
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateTreeNotExists() throws FileNotFoundException {
    CommandLine cmd = validateTreeInit("non_existent.json");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  private CommandLine validateTemplateConfigInit(String templateConfigPathString)
      throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    CommandLine cmd = mock(CommandLine.class);
    doReturn(false).when(cmd).hasOption(OPT_MAX_SAMPLES);
    doReturn(false).when(cmd).hasOption(OPT_PHENOTYPES);
    doReturn(false).when(cmd).hasOption(OPT_OUTPUT);
    doReturn(true).when(cmd).hasOption(OPT_FORCE);
    doReturn(false).when(cmd).hasOption(OPT_PED);
    doReturn(false).when(cmd).hasOption(OPT_REFERENCE);
    doReturn(false).when(cmd).hasOption(OPT_GENES);
    doReturn(false).when(cmd).hasOption(OPT_CRAM);
    doReturn(false).when(cmd).hasOption(OPT_TREE);
    doReturn(true).when(cmd).hasOption(OPT_TEMPLATE_CONFIG);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);

    doReturn(templateConfigPathString).when(cmd).getOptionValue(OPT_TEMPLATE_CONFIG);
    doReturn(templateFile).when(cmd).getOptionValue(OPT_TEMPLATE);

    return cmd;
  }

  @Test
  void validateTemplateConfigValidValue() throws FileNotFoundException {
    String treePath = ResourceUtils.getFile("classpath:template_config.json").toString();
    CommandLine cmd = validateTemplateConfigInit(treePath);
    assertDoesNotThrow(() -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateTemplateConfigInvalidFileTypeValue() throws FileNotFoundException {
    String inputFile = ResourceUtils.getFile("classpath:exampleCmdline.vcf").toString();
    CommandLine cmd = validateTemplateConfigInit(inputFile);
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }

  @Test
  void validateTemplateConfigNotExists() throws FileNotFoundException {
    CommandLine cmd = validateTemplateConfigInit("non_existent.json");
    assertThrows(
        IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));
  }
}
