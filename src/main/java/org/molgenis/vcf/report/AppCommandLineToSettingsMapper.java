package org.molgenis.vcf.report;

import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_CRAM;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_GENES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_MAX_SAMPLES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_METADATA;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PHENOTYPES;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PROBANDS;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_REFERENCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_SAMPLE_TREE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE_CONFIG;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TREE;
import static org.molgenis.vcf.report.utils.PathUtils.parsePaths;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.ReportWriterSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.generator.SampleSettings.CramPath;
import org.molgenis.vcf.report.generator.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

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
    String templateValue = commandLine.getOptionValue(OPT_TEMPLATE);
    templatePath = Path.of(templateValue);

    Path metadataPath;
    if (commandLine.hasOption(OPT_METADATA)) {
      String metadataPathValue = commandLine.getOptionValue(OPT_METADATA);
      metadataPath = Path.of(metadataPathValue);
    } else {
      metadataPath = null;
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

    Path sampleTreePath;
    if (commandLine.hasOption(OPT_SAMPLE_TREE)) {
      String sampleTreePathValue = commandLine.getOptionValue(OPT_SAMPLE_TREE);
      sampleTreePath = Path.of(sampleTreePathValue);
    } else {
      sampleTreePath = null;
    }

    Path templateConfigPath;
    if (commandLine.hasOption(OPT_TEMPLATE_CONFIG)) {
      String templateConfigValue = commandLine.getOptionValue(OPT_TEMPLATE_CONFIG);
      templateConfigPath = Path.of(templateConfigValue);
    } else {
      templateConfigPath = null;
    }

    Map<String, CramPath> cramPaths;
    if (commandLine.hasOption(OPT_CRAM)) {
      String cramPathValue = commandLine.getOptionValue(OPT_CRAM);
      cramPaths = toCramPaths(cramPathValue);
    } else {
      cramPaths = Map.of();
    }

    boolean overwriteOutput = commandLine.hasOption(OPT_FORCE);

    boolean debugMode = commandLine.hasOption(OPT_DEBUG);

    String appArgs = String.join(" ", args);
    Path sqlWasmPath;
    try {
      sqlWasmPath = ResourceUtils.getFile("classpath:sql-wasm-1.13.wasm").toPath();
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName,
            appVersion,
            appArgs,
            maxSamples,
            metadataPath,
            sqlWasmPath,
            referencePath,
            genesPath,
            decisionTreePath,
            sampleTreePath,
            templateConfigPath);
    ReportWriterSettings reportWriterSettings = new ReportWriterSettings(templatePath, debugMode);
    SampleSettings sampleSettings =
        new SampleSettings(probandNames, pedPaths, phenotypes, cramPaths);
    return new Settings(
        inputPath,
        reportGeneratorSettings,
        outputPath,
        overwriteOutput,
        reportWriterSettings,
        sampleSettings);
  }

  private Map<String, CramPath> toCramPaths(String cramPathValue) {
    Map<String, CramPath> cramPaths = new LinkedHashMap<>();
    String[] tokens = cramPathValue.split(",");
    for (String token : tokens) {
      int idx = token.indexOf("=");
      String sampleId = token.substring(0, idx);
      String cramStr = token.substring(idx + 1);
      Path cramPath = Path.of(cramStr);
      Path craiPath = Path.of(cramStr + ".crai");
      cramPaths.put(sampleId, new CramPath(cramPath, craiPath));
    }
    return cramPaths;
  }
}
