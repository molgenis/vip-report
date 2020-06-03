package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir Path sharedTempDir;

  @Test
  void test() throws IOException, JSONException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String pedFile = ResourceUtils.getFile("classpath:example.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();

    String[] args = {"-i", inputFile, "-o", outputFile, "-t", templateFile};
    SpringApplication.run(App.class, args);

    String report = Files.readString(Path.of(outputFile));

    Path expectedReportPath = ResourceUtils.getFile("classpath:example.vcf.html").toPath();
    String expectedReport =
        Files.readString(expectedReportPath)
            .replace("{{ inputPath }}", inputFile.replace("\\", "\\\\"))
            .replace("{{ outputPath }}", outputFile.replace("\\", "\\\\"))
            .replace("{{ templatePath }}", templateFile.replace("\\", "\\\\"));

    //check the report api value with JSONAssert
    String actualApi = getElementValue(report, "script");
    String expectedApi = getElementValue(expectedReport, "script");
    JSONAssert.assertEquals(
        expectedApi.replace("window.api = ", ""), actualApi.replace("window.api = ", ""), false);

    //check the rest of the report
    assertEquals(expectedReport.replace(expectedApi,"[API_VALUE]"), report.replace(actualApi, "[API_VALUE]"));
  }

  private String getElementValue(String html, String elementName) {
    Pattern p = Pattern.compile("<" + elementName + ">(.*)</" + elementName + ">");
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
