package org.molgenis.vcf.report.generator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.vcf.report.helpers.jackson.phenopacket.ObjectMapperConfigurer.configure;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.molgenis.vcf.report.helpers.jackson.phenopacket.IndividualMixin;
import org.molgenis.vcf.report.helpers.jackson.phenopacket.PhenopacketModelMixin;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.helpers.jackson.phenopacket.PhenopacketInoreSuperIntrospector;
import org.molgenis.vcf.report.helpers.jackson.phenopacket.PersonMixin;
import org.molgenis.vcf.report.helpers.jackson.phenopacket.PhenopacketMixin;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Age;
import org.phenopackets.schema.v1.core.AgeRange;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.MetaData;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.Pedigree.Person;
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
    String templateHtml;
    Path templatePath = reportWriterSettings.getTemplatePath();
    if (templatePath != null) {
      LOGGER.info("creating report using template {}", templatePath);
      templateHtml = Files.readString(templatePath, UTF_8);
    } else {
      LOGGER.info("creating report using default template");
      try (InputStream inputStream =
          new ClassPathResource("vip-report-template.html").getInputStream()) {
        templateHtml = new String(inputStream.readAllBytes(), UTF_8);
      }
    }

    String scriptTag = createScriptTag(report, reportWriterSettings.isPrettyPrint());
    String reportHtml = templateHtml.replace("</head>", scriptTag + "</head>");

    Files.writeString(outputFile, reportHtml, UTF_8);
  }

  private String createScriptTag(Report report, boolean prettyPrint)
      throws JsonProcessingException {
    ObjectMapper objectMapper =
        new ObjectMapper();
    configure(objectMapper);
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
