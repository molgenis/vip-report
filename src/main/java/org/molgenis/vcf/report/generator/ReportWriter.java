package org.molgenis.vcf.report.generator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReportWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportWriter.class);
  private final Base85Encoder base85Encoder;

  public ReportWriter(Base85Encoder base85Encoder) {
    this.base85Encoder = requireNonNull(base85Encoder);
  }

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
    String templateHtml;
    Path templatePath = reportWriterSettings.getTemplatePath();

    LOGGER.info("creating report using template {}", templatePath);
    templateHtml = Files.readString(templatePath, UTF_8);

    String scriptTag = createScriptTag(report, reportWriterSettings.isPrettyPrint());

    String reportHtml =
        templateHtml.contains("<script")
            ? templateHtml.replace("</title>", "</title>" + scriptTag)
            : templateHtml.replace("</head>", scriptTag + "</head>");

    Files.writeString(outputFile, reportHtml, UTF_8);
  }

  private String createScriptTag(Report report, boolean prettyPrint)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(Include.ALWAYS);
    String json;
    if (prettyPrint) {
      json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
    } else {
      json = objectMapper.writeValueAsString(report);
    }
    String str = "window.api=" + json;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (BestCompressionGZIPOutputStream outputStream =
        new BestCompressionGZIPOutputStream(byteArrayOutputStream)) {
      outputStream.write(str.getBytes(UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return "<script type=\"application/gzip\" class=\"ldr-js\">"
        + base85Encoder.encode(byteArrayOutputStream.toByteArray())
        + "</script>";
  }
}
