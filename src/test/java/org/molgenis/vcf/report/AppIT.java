package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir Path sharedTempDir;

  @Test
  void test() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
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

    assertEquals(expectedReport, report);
  }
}
