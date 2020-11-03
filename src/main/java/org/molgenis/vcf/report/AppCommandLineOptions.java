package org.molgenis.vcf.report;

import static java.lang.String.format;
import static org.molgenis.vcf.report.mapper.PhenopacketMapper.PHENOTYPE_SEPARATOR;
import static org.molgenis.vcf.report.mapper.PhenopacketMapper.SAMPLE_PHENOTYPE_SEPARATOR;
import static org.molgenis.vcf.report.mapper.PhenopacketMapper.checkPhenotype;
import static org.molgenis.vcf.report.utils.PathUtils.parsePaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.utils.InvalidSamplePhenotypesException;

class AppCommandLineOptions {

  static final String OPT_INPUT = "i";
  static final String OPT_INPUT_LONG = "input";
  static final String OPT_OUTPUT = "o";
  static final String OPT_OUTPUT_LONG = "output";
  static final String OPT_TEMPLATE = "t";
  static final String OPT_TEMPLATE_LONG = "template";
  static final String OPT_PROBANDS = "pb";
  static final String OPT_PROBANDS_LONG = "probands";
  static final String OPT_PED = "pd";
  static final String OPT_PED_LONG = "pedigree";
  static final String OPT_PHENOTYPES = "ph";
  static final String OPT_PHENOTYPES_LONG = "phenotypes";
  static final String OPT_MAX_RECORDS = "mr";
  static final String OPT_MAX_RECORDS_LONG = "max_records";
  static final String OPT_MAX_SAMPLES = "ms";
  static final String OPT_MAX_SAMPLES_LONG = "max_samples";
  static final String OPT_FORCE = "f";
  static final String OPT_FORCE_LONG = "force";
  static final String OPT_DEBUG = "d";
  static final String OPT_DEBUG_LONG = "debug";
  static final String OPT_VERSION = "v";
  static final String OPT_VERSION_LONG = "version";
  private static final Options APP_OPTIONS;
  private static final Options APP_VERSION_OPTIONS;

  static {
    Options appOptions = new Options();
    appOptions.addOption(
        Option.builder(OPT_INPUT)
            .hasArg(true)
            .required()
            .longOpt(OPT_INPUT_LONG)
            .desc("Input VCF file (.vcf or .vcf.gz).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_OUTPUT)
            .hasArg(true)
            .longOpt(OPT_OUTPUT_LONG)
            .desc("Output report file (.html).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_FORCE)
            .longOpt(OPT_FORCE_LONG)
            .desc("Override the output file if it already exists.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_TEMPLATE)
            .hasArg(true)
            .longOpt(OPT_TEMPLATE_LONG)
            .desc("Report template file (.html).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_PROBANDS)
            .hasArg(true)
            .longOpt(OPT_PROBANDS_LONG)
            .desc("Comma-separated list of proband names.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_PED)
            .hasArg(true)
            .longOpt(OPT_PED_LONG)
            .desc("Comma-separated list of pedigree files (.ped).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_PHENOTYPES)
            .hasArg(true)
            .longOpt(OPT_PHENOTYPES_LONG)
            .desc(
                "Comma-separated list of sample-phenotypes (e.g. HP:123 or HP:123;HP:234 or sample0/HP:123,sample1/HP:234). Phenotypes are CURIE formatted (prefix:reference) and separated by a semicolon.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_MAX_RECORDS)
            .hasArg(true)
            .longOpt(OPT_MAX_RECORDS_LONG)
            .desc(
                format(
                    "Integer stating the maximum number of records to be available in the report. Default: %s",
                    ReportGeneratorSettings.DEFAULT_MAX_NR_RECORDS))
            .build());
    appOptions.addOption(
        Option.builder(OPT_MAX_SAMPLES)
            .hasArg(true)
            .longOpt(OPT_MAX_SAMPLES_LONG)
            .desc(
                format(
                    "Integer stating the maximum number of samples to be available in the report. Default: %s",
                    ReportGeneratorSettings.DEFAULT_MAX_NR_SAMPLES))
            .build());
    appOptions.addOption(
        Option.builder(OPT_DEBUG)
            .longOpt(OPT_DEBUG_LONG)
            .desc("Enable debug mode (additional logging and pretty printed report.")
            .build());
    APP_OPTIONS = appOptions;
    Options appVersionOptions = new Options();
    appVersionOptions.addOption(
        Option.builder(OPT_VERSION)
            .required()
            .longOpt(OPT_VERSION_LONG)
            .desc("Print version.")
            .build());
    APP_VERSION_OPTIONS = appVersionOptions;
  }

  private AppCommandLineOptions() {}

  static Options getAppOptions() {
    return APP_OPTIONS;
  }

  static Options getAppVersionOptions() {
    return APP_VERSION_OPTIONS;
  }

  static void validateCommandLine(CommandLine commandLine) {
    validateInput(commandLine);
    validateOutput(commandLine);
    validateTemplate(commandLine);
    validateProbands(commandLine);
    validatePed(commandLine);
    validatePhenotypes(commandLine);
    validateMaxRecords(commandLine);
    validateMaxSamples(commandLine);
  }

  private static void validateMaxSamples(CommandLine commandLine) {
    validateInteger(commandLine, OPT_MAX_SAMPLES);
  }

  private static void validateMaxRecords(CommandLine commandLine) {
    validateInteger(commandLine, OPT_MAX_RECORDS);
  }

  private static void validateInteger(CommandLine commandLine, String option) {
    if (!commandLine.hasOption(option)) {
      return;
    }
    String maxSamplesString = commandLine.getOptionValue(option);
    try {
      int value = Integer.parseInt(maxSamplesString);
      if (value < 0) {
        throw new InvalidIntegerException(option, maxSamplesString);
      }
    } catch (NumberFormatException e) {
      throw new InvalidIntegerException(option, maxSamplesString);
    }
  }

  private static void validatePhenotypes(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_PHENOTYPES)) {
      return;
    }
    String phenotypesString = commandLine.getOptionValue(OPT_PHENOTYPES);
    if (phenotypesString.contains(SAMPLE_PHENOTYPE_SEPARATOR)) {
      for (String samplePhenotypes : phenotypesString.split(",")) {
        if (samplePhenotypes.contains("/")) {
          if (samplePhenotypes.split("/").length != 2) {
            throw new InvalidSamplePhenotypesException(samplePhenotypes);
          }
        } else {
          throw new MixedPhenotypesException();
        }
      }
    } else {
      String[] phenotypes = phenotypesString.split(PHENOTYPE_SEPARATOR);
      for (String phenotype : phenotypes) {
        checkPhenotype(phenotype);
      }
    }
  }

  private static void validateInput(CommandLine commandLine) {
    Path inputPath = Path.of(commandLine.getOptionValue(OPT_INPUT));
    if (!Files.exists(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' does not exist.", inputPath.toString()));
    }
    if (Files.isDirectory(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is a directory.", inputPath.toString()));
    }
    if (!Files.isReadable(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not readable.", inputPath.toString()));
    }
    String inputPathStr = inputPath.toString();
    if (!inputPathStr.endsWith(".vcf") && !inputPathStr.endsWith(".vcf.gz")) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not a .vcf or .vcf.gz file.", inputPathStr));
    }
  }

  private static void validateOutput(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_OUTPUT)) {
      return;
    }

    Path outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));

    String outputPathStr = outputPath.toString();
    if (!outputPathStr.endsWith(".html")) {
      throw new IllegalArgumentException(
          format("Output file '%s' is not a .html file.", outputPathStr));
    }

    if (!commandLine.hasOption(OPT_FORCE) && Files.exists(outputPath)) {
      throw new IllegalArgumentException(
          format("Output file '%s' already exists", outputPath.toString()));
    }
  }

  private static void validateTemplate(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_TEMPLATE)) {
      return;
    }

    Path templatePath = Path.of(commandLine.getOptionValue(OPT_TEMPLATE));
    if (!Files.exists(templatePath)) {
      throw new IllegalArgumentException(
          format("Template file '%s' does not exist.", templatePath.toString()));
    }
    if (Files.isDirectory(templatePath)) {
      throw new IllegalArgumentException(
          format("Template file '%s' is a directory.", templatePath.toString()));
    }
    if (!Files.isReadable(templatePath)) {
      throw new IllegalArgumentException(
          format("Template file '%s' is not readable.", templatePath.toString()));
    }
    String templatePathStr = templatePath.toString();
    if (!templatePathStr.endsWith(".html")) {
      throw new IllegalArgumentException(
          format("Template file '%s' is not a .html file.", templatePathStr));
    }
  }

  private static void validateProbands(@SuppressWarnings("unused") CommandLine commandLine) {
    // no op
  }

  private static void validatePed(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_PED)) {
      return;
    }
    List<Path> pedPaths = parsePaths(commandLine.getOptionValue(OPT_PED));
    for (Path pedPath : pedPaths) {
      if (!Files.exists(pedPath)) {
        throw new IllegalArgumentException(
            format("Ped file '%s' does not exist.", pedPath.toString()));
      }
      if (Files.isDirectory(pedPath)) {
        throw new IllegalArgumentException(
            format("Ped file '%s' is a directory.", pedPath.toString()));
      }
      if (!Files.isReadable(pedPath)) {
        throw new IllegalArgumentException(
            format("Ped file '%s' is not readable.", pedPath.toString()));
      }
      String templatePathStr = pedPath.toString();
      if (!templatePathStr.endsWith(".ped")) {
        throw new IllegalArgumentException(
            format("Ped file '%s' is not a .ped file.", templatePathStr));
      }
    }
  }
}
