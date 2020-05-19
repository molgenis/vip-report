package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.util.List;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.ReportMetadata;
import org.molgenis.vcf.report.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {

  private final HtsJdkMapper htsJdkMapper;

  public ReportGenerator(HtsJdkMapper htsJdkMapper) {
    this.htsJdkMapper = requireNonNull(htsJdkMapper);
  }

  public Report generateReport(Path inputVcfPath, ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      report = createReport(vcfFileReader, reportGeneratorSettings);
    }
    return report;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }

  private Report createReport(
      VCFFileReader vcfFileReader, ReportGeneratorSettings reportGeneratorSettings) {
    Items<Sample> samples = createSamples(vcfFileReader, reportGeneratorSettings);
    Items<Record> records =
        createRecords(vcfFileReader, reportGeneratorSettings, samples.getItems());
    ReportMetadata reportMetadata =
        new ReportMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportData reportData = new ReportData(samples, records);
    return new Report(reportMetadata, reportData);
  }

  private Items<Sample> createSamples(
      VCFFileReader vcfFileReader, ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    return htsJdkMapper.mapSamples(fileHeader, maxNrSamples);
  }

  private Items<Record> createRecords(
      VCFFileReader vcfFileReader,
      ReportGeneratorSettings reportGeneratorSettings,
      List<Sample> samples) {
    int maxNrRecords = reportGeneratorSettings.getMaxNrRecords();
    return htsJdkMapper.mapRecords(vcfFileReader, maxNrRecords, samples);
  }
}
