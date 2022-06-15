package org.molgenis.vcf.report;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    String pedFiles =
        ResourceUtils.getFile("classpath:example.ped").toString()
            + ","
            + ResourceUtils.getFile("classpath:example2.ped").toString();
    String outputFile = sharedTempDir.resolve("example.vcf.html").toString();
    String templateFile = ResourceUtils.getFile("classpath:example-template.html").toString();
    String probands = "NA00001";
    String phenotypes = "Jimmy/HP:123456,Unknown/test:Headache,Jane/OMIM:23456";
    String bamFile = ResourceUtils.getFile("classpath:example.bam").toString();

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
      "-d",
      "-b",
      "NA00001=" + bamFile
    };
    SpringApplication.run(App.class, args);

    String report = Files.readString(Path.of(outputFile));
    // due to report content encoding we only check whether the application ran without error and a
    // report was created
    assertNotNull(report);
  }
}
