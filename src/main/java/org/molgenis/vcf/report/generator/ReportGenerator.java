package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.mapper.PhenopacketMapper.createPhenopackets;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.ReportMetadata;
import org.molgenis.vcf.report.utils.PedReader;
import org.molgenis.vcf.report.utils.PedToPersonsParser;
import org.molgenis.vcf.report.utils.PersonListMerger;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {
  private final HtsJdkMapper htsJdkMapper;

  public ReportGenerator(HtsJdkMapper htsJdkMapper) {
    this.htsJdkMapper = requireNonNull(htsJdkMapper);
  }

  public Report generateReport(
      Path inputVcfPath,
      Path pedigreePath,
      String phenotypes,
      ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      report = createReport(vcfFileReader, pedigreePath, phenotypes, reportGeneratorSettings);
    }
    return report;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }

  private Report createReport(
      VCFFileReader vcfFileReader,
      Path pedigreePath,
      String phenotypes,
      ReportGeneratorSettings reportGeneratorSettings) {
    Items<Person> persons = createPersons(vcfFileReader, pedigreePath, reportGeneratorSettings);
    Items<Phenopacket> phenopackets = createPhenopackets(phenotypes, persons.getItems());
    Items<Record> records =
        createRecords(vcfFileReader, reportGeneratorSettings, persons.getItems());
    ReportMetadata reportMetadata =
        new ReportMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportData reportData = new ReportData(persons, phenopackets, records);
    return new Report(reportMetadata, reportData);
  }

  private Items<Person> createPersons(
      VCFFileReader vcfFileReader, Path pedigreePath, ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    Items<Person> persons = htsJdkMapper.mapSamples(fileHeader, maxNrSamples);
    if (pedigreePath != null) {
      try (PedReader reader = new PedReader(new FileReader(pedigreePath.toFile()))) {
        final Map<String, Person> pedigreePersons = PedToPersonsParser.parse(reader, maxNrSamples);
        persons = PersonListMerger.merge(maxNrSamples, persons.getItems(), pedigreePersons);
      } catch (IOException e) {
        // this should never happen since the file was validated in the AppCommandLineOptions
        throw new IllegalStateException(e);
      }
    }
    return persons;
  }

  private Items<Record> createRecords(
      VCFFileReader vcfFileReader,
      ReportGeneratorSettings reportGeneratorSettings,
      List<Person> samples) {
    int maxNrRecords = reportGeneratorSettings.getMaxNrRecords();
    return htsJdkMapper.mapRecords(vcfFileReader, maxNrRecords, samples);
  }
}
