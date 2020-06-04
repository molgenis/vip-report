package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
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
  void validateCommandLineInputNotExists() throws FileNotFoundException {
    String inputFile = "notexists.vcf";

    CommandLine cmd = mock(CommandLine.class);
    doReturn(inputFile).when(cmd).getOptionValue(OPT_INPUT);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> AppCommandLineOptions.validateCommandLine(cmd));

    assertEquals("Input file 'notexists.vcf' does not exist.", exception.getMessage());
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
}