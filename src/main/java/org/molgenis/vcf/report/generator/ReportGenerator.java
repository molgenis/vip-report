package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.mapper.HtsFileMapper;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.mapper.PedToSamplesMapper;
import org.molgenis.vcf.report.mapper.PhenopacketMapper;
import org.molgenis.vcf.report.model.metadata.HtsFile;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Phenopacket;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.report.utils.PersonListMerger;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {
  private final HtsJdkMapper htsJdkMapper;
  private final PhenopacketMapper phenopacketMapper;
  private final PedToSamplesMapper pedToSamplesMapper;
  private final PersonListMerger personListMerger;
  private final HtsFileMapper htsFileMapper;

  public ReportGenerator(
      HtsJdkMapper htsJdkMapper,
      PhenopacketMapper phenopacketMapper,
      PedToSamplesMapper pedToSamplesMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper) {
    this.htsJdkMapper = requireNonNull(htsJdkMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.pedToSamplesMapper = requireNonNull(pedToSamplesMapper);
    this.personListMerger = requireNonNull(personListMerger);
    this.htsFileMapper = requireNonNull(htsFileMapper);
  }

  public Report generateReport(
      Path inputVcfPath,
      List<Path> pedigreePaths,
      String phenotypes,
      ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      report =
          createReport(
              vcfFileReader, inputVcfPath, pedigreePaths, phenotypes, reportGeneratorSettings);
    }
    return report;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }

  private Report createReport(
      VCFFileReader vcfFileReader,
      Path vcfPath,
      List<Path> pedigreePaths,
      String phenotypes,
      ReportGeneratorSettings reportGeneratorSettings) {
    HtsFile htsFile = htsFileMapper.map(vcfFileReader.getFileHeader(), vcfPath.toString());

    Items<Sample> samples = createPersons(vcfFileReader, pedigreePaths, reportGeneratorSettings);

    Items<Phenopacket> phenopackets;
    if (phenotypes != null && !phenotypes.isEmpty()) {
      phenopackets = phenopacketMapper.mapPhenotypes(phenotypes, samples.getItems());
    } else {
      phenopackets = new Items<>(Collections.emptyList(), 0);
    }

    RecordsMetadata recordsMetadata =
        htsJdkMapper.mapRecordsMetadata(vcfFileReader.getFileHeader());
    Items<Record> records =
        createRecords(recordsMetadata, vcfFileReader, reportGeneratorSettings, samples.getItems());
    AppMetadata appMetadata =
        new AppMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportMetadata reportMetadata = new ReportMetadata(appMetadata, htsFile, recordsMetadata);
    ReportData reportData = new ReportData(samples, phenopackets, records);
    return new Report(reportMetadata, reportData);
  }

  private Items<Sample> createPersons(
      VCFFileReader vcfFileReader, List<Path> pedigreePaths, ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    Items<Sample> samples = htsJdkMapper.mapSamples(fileHeader, maxNrSamples);
    if (pedigreePaths != null) {
      final Map<String, Sample> pedigreePersons =
          pedToSamplesMapper.mapPedFileToPersons(pedigreePaths, maxNrSamples);
      samples = personListMerger.merge(samples.getItems(), pedigreePersons, maxNrSamples);
    }
    return samples;
  }

  private Items<Record> createRecords(
      RecordsMetadata recordsMetadata,
      Iterable<VariantContext> variantContexts,
      ReportGeneratorSettings reportGeneratorSettings,
      List<Sample> samples) {
    int maxNrRecords = reportGeneratorSettings.getMaxNrRecords();
    return htsJdkMapper.mapRecords(recordsMetadata, variantContexts, maxNrRecords, samples);
  }
}
