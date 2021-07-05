package org.molgenis.vcf.report;

import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_BAM;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_GENES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_RECORDS;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_SAMPLES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PHENOTYPES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PROBANDS;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_REFERENCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TREE;
import static org.molgenis.vcf.report.utils.PathUtils.parsePaths;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.ReportWriterSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.generator.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppCommandLineToSettingsMapper {

  private final String appName;
  private final String appVersion;

  AppCommandLineToSettingsMapper(
      @Value("${app.name}") String appName, @Value("${app.version}") String appVersion) {
    this.appName = appName;
    this.appVersion = appVersion;
  }

  Settings map(CommandLine commandLine, String... args) {
    String inputPathValue = commandLine.getOptionValue(OPT_INPUT);
    Path inputPath = Path.of(inputPathValue);

    Path templatePath;
    if (commandLine.hasOption(OPT_TEMPLATE)) {
      String templateValue = commandLine.getOptionValue(OPT_TEMPLATE);
      templatePath = Path.of(templateValue);
    } else {
      templatePath = null;
    }

    Path outputPath;
    if (commandLine.hasOption(OPT_OUTPUT)) {
      outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));
    } else {
      outputPath = Path.of(commandLine.getOptionValue(OPT_INPUT) + ".html");
    }

    List<String> probandNames;
    if (commandLine.hasOption(OPT_PROBANDS)) {
      probandNames = Arrays.asList(commandLine.getOptionValue(OPT_PROBANDS).split(","));
    } else {
      probandNames = List.of();
    }

    List<Path> pedPaths;
    if (commandLine.hasOption(OPT_PED)) {
      pedPaths = parsePaths(commandLine.getOptionValue(OPT_PED));
    } else {
      pedPaths = null;
    }

    String phenotypes;
    if (commandLine.hasOption(OPT_PHENOTYPES)) {
      phenotypes = commandLine.getOptionValue(OPT_PHENOTYPES);
    } else {
      phenotypes = null;
    }

    int maxRecords;
    if (commandLine.hasOption(OPT_MAX_RECORDS)) {
      maxRecords = Integer.parseInt(commandLine.getOptionValue(OPT_MAX_RECORDS));
    } else {
      maxRecords = ReportGeneratorSettings.DEFAULT_MAX_NR_RECORDS;
    }

    int maxSamples;
    if (commandLine.hasOption(OPT_MAX_SAMPLES)) {
      maxSamples = Integer.parseInt(commandLine.getOptionValue(OPT_MAX_SAMPLES));
    } else {
      maxSamples = ReportGeneratorSettings.DEFAULT_MAX_NR_SAMPLES;
    }

    Path referencePath;
    if (commandLine.hasOption(OPT_REFERENCE)) {
      String referencePathValue = commandLine.getOptionValue(OPT_REFERENCE);
      referencePath = Path.of(referencePathValue);
    } else {
      referencePath = null;
    }

    Path genesPath;
    if (commandLine.hasOption(OPT_GENES)) {
      String genesPathValue = commandLine.getOptionValue(OPT_GENES);
      genesPath = Path.of(genesPathValue);
    } else {
      genesPath = null;
    }

    Path decisionTreePath;
    if (commandLine.hasOption(OPT_TREE)) {
      String decisionTreePathValue = commandLine.getOptionValue(OPT_TREE);
      decisionTreePath = Path.of(decisionTreePathValue);
    } else {
      decisionTreePath = null;
    }

    Map<String, Path> bamPaths;
    if (commandLine.hasOption(OPT_BAM)) {
      String bamPathValue = commandLine.getOptionValue(OPT_BAM);
      bamPaths = toBamPaths(bamPathValue);
    } else {
      bamPaths = Map.of();
    }

    boolean overwriteOutput = commandLine.hasOption(OPT_FORCE);

    boolean debugMode = commandLine.hasOption(OPT_DEBUG);

    String appArgs = String.join(" ", args);
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName, appVersion, appArgs, maxSamples, maxRecords, referencePath, genesPath, decisionTreePath);
    ReportWriterSettings reportWriterSettings = new ReportWriterSettings(templatePath, debugMode);
    SampleSettings sampleSettings =
        new SampleSettings(probandNames, pedPaths, phenotypes, bamPaths);
    return new Settings(
        inputPath,
        reportGeneratorSettings,
        outputPath,
        overwriteOutput,
        reportWriterSettings,
        sampleSettings);
  }

  private Map<String, Path> toBamPaths(String bamPathValue) {
    Map<String, Path> bamPaths = new LinkedHashMap<>();
    String[] tokens = bamPathValue.split(",");
    for (String token : tokens) {
      int idx = token.indexOf("=");
      String sampleId = token.substring(0, idx);
      Path bamPath = Path.of(token.substring(idx + 1));
      bamPaths.put(sampleId, bamPath);
    }
    return bamPaths;
  }
}
