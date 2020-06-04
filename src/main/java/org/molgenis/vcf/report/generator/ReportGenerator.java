package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.util.Strings;
import org.molgenis.vcf.report.mapper.HtsFileMapper;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.mapper.PedToPersonMapper;
import org.molgenis.vcf.report.mapper.PhenopacketMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.ReportMetadata;
import org.molgenis.vcf.report.utils.PersonListMerger;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {
  private final HtsJdkMapper htsJdkMapper;
  private final PhenopacketMapper phenopacketMapper;
  private final PedToPersonMapper pedToPersonMapper;
  private final PersonListMerger personListMerger;
  private final HtsFileMapper htsFileMapper;

  public ReportGenerator(
      HtsJdkMapper htsJdkMapper,
      PhenopacketMapper phenopacketMapper,
      PedToPersonMapper pedToPersonMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper) {
    this.htsJdkMapper = requireNonNull(htsJdkMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.pedToPersonMapper = requireNonNull(pedToPersonMapper);
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

    Items<Person> persons = createVcfPersons(vcfFileReader, reportGeneratorSettings);
    HtsFile htsFile = htsFileMapper.map(vcfFileReader.getFileHeader(), vcfPath.toString(), persons);
    if (pedigreePaths != null) {
      persons = createPersons(persons, pedigreePaths, reportGeneratorSettings);
    }

    Items<Phenopacket> phenopackets;
    if (!Strings.isEmpty(phenotypes)) {
      phenopackets = phenopacketMapper.mapPhenotypes(phenotypes, persons.getItems());
    } else {
      phenopackets = new Items<>(Collections.emptyList(), 0);
    }

    Items<Record> records =
        createRecords(vcfFileReader, reportGeneratorSettings, persons.getItems());
    ReportMetadata reportMetadata =
        new ReportMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportData reportData = new ReportData(htsFile, persons, phenopackets, records);
    return new Report(reportMetadata, reportData);
  }

  private Items<Person> createVcfPersons(
      VCFFileReader vcfFileReader, ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    return htsJdkMapper.mapSamples(fileHeader, maxNrSamples);
  }

  private Items<Person> createPersons(
      Items<Person> persons, List<Path> pedigreePaths, ReportGeneratorSettings settings) {
    int maxNrSamples = settings.getMaxNrSamples();
    final Map<String, Person> pedigreePersons =
        pedToPersonMapper.mapPedFileToPersons(pedigreePaths, maxNrSamples);
    persons = personListMerger.merge(persons.getItems(), pedigreePersons, maxNrSamples);
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
