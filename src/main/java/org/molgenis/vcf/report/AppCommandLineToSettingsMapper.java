package org.molgenis.vcf.report;

import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_TEMPLATE;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PED;
import static org.molgenis.vcf.report.AppCommandLineOptions.OPT_PHENOTYPES;
import static org.molgenis.vcf.report.utils.PathUtils.parsePaths;

import java.nio.file.Path;
import java.util.List;
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

    boolean overwriteOutput = commandLine.hasOption(OPT_FORCE);

    boolean debugMode = commandLine.hasOption(OPT_DEBUG);

    String appArgs = String.join(" ", args);
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName,
            appVersion,
            appArgs,
            ReportGeneratorSettings.DEFAULT_MAX_NR_SAMPLES,
            ReportGeneratorSettings.DEFAULT_MAX_NR_RECORDS);
    ReportWriterSettings reportWriterSettings = new ReportWriterSettings(templatePath, debugMode);
    SampleSettings sampleSettings = new SampleSettings(pedPaths, phenotypes);
    return new Settings(
        inputPath,
        reportGeneratorSettings,
        outputPath,
        overwriteOutput,
        reportWriterSettings,
        sampleSettings);
  }
}
