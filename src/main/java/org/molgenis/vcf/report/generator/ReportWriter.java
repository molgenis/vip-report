package org.molgenis.vcf.report.generator;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.molgenis.vcf.report.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ReportWriter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReportWriter.class);

  public void write(Report report, Path outputFile, ReportWriterSettings reportWriterSettings) {
    try {
      writeCheckedException(report, outputFile, reportWriterSettings);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeCheckedException(
      Report report, Path outputFile, ReportWriterSettings reportWriterSettings)
      throws IOException {
    if (reportWriterSettings.isOverwriteOutputReport()) {
      Files.deleteIfExists(outputFile);
    }

    String templateHtml;
    Path templatePath = reportWriterSettings.getTemplatePath();
    if (templatePath != null) {
      LOGGER.info("creating report using template {}", templatePath);
      templateHtml = Files.readString(templatePath, UTF_8);
    } else {
      LOGGER.info("creating report using default template");
      try (InputStream inputStream =
          new ClassPathResource("template-default.html").getInputStream()) {
        templateHtml = new String(inputStream.readAllBytes(), UTF_8);
      }
    }
    String scriptTag = createScriptTag(report, reportWriterSettings.isPrettyPrint());
    String reportHtml = templateHtml.replace("</head>", scriptTag + "</head>");

    Files.writeString(outputFile, reportHtml, UTF_8);
  }

  private String createScriptTag(Report report, boolean prettyPrint)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);
    String json;
    if (prettyPrint) {
      json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
    } else {
      json = objectMapper.writeValueAsString(report);
    }
    return "<script>window.api = " + json + "</script>";
  }
}
