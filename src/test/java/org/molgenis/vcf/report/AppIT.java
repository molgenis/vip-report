package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir Path sharedTempDir;

  @Test
  void test() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();
    String probands = "NA00001";
    String phenotypes = "Jimmy/HP:123456,Unknown/test:Headache,Jane/OMIM:23456";

    String[] args = {
        "-i",
        inputFile,
        "-o",
        outputFile,
        "-t",
        templateFile,
        "-pb",
        probands,
        "-pd",
        pedFiles,
        "-ph",
        phenotypes,
        "-d"
    };
    SpringApplication.run(App.class, args);

    String report = Files.readString(Path.of(outputFile));

    Path expectedReportPath = ResourceUtils.getFile("classpath:example.vcf.html").toPath();
    String expectedReport =
        Files.readString(expectedReportPath)
            .replace("{{ inputPath }}", inputFile.replace("\\", "\\\\"))
            .replace("{{ pedPaths }}", pedFiles.replace("\\", "\\\\"))
            .replace("{{ outputPath }}", outputFile.replace("\\", "\\\\"))
            .replace("{{ templatePath }}", templateFile.replace("\\", "\\\\"));

    // check the report api value with JSONAssert
    String actualApi = getElementValue(report, "script");
    String expectedApi = getElementValue(expectedReport, "script");

    assertAll(
        () ->
            JSONAssert.assertEquals(
                expectedApi.replace("window.api = ", "").replaceAll("\"vcfGz\" : \".*?\"",
                    "\"vcfGz\" : \"data_that_differs_per_os_due_to_gzip\""),
                actualApi.replace("window.api = ", "").replaceAll("\"vcfGz\" : \".*?\"",
                    "\"vcfGz\" : \"data_that_differs_per_os_due_to_gzip\""),
                true),
        // check the rest of the report
        () ->
            assertEquals(
                expectedReport.replace(expectedApi, "[API_VALUE]"),
                report.replace(actualApi, "[API_VALUE]")));
  }

  private String getElementValue(String html, String elementName) {
    Pattern p = Pattern.compile("<" + elementName + ">(.*)</" + elementName + ">", Pattern.DOTALL);
    Matcher m = p.matcher(html);
    String result = "";
    if (m.find()) {
      result = m.group(1);
    } else {
      fail(String.format("Element '%s' not found in the html value.", elementName));
    }
    return result;
  }
}
