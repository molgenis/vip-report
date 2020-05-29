package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Phenotype;
import org.molgenis.vcf.report.model.PhenotypeMode;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.ReportMetadata;
import org.molgenis.vcf.report.model.SamplePhenotype;
import org.molgenis.vcf.report.utils.PedReader;
import org.molgenis.vcf.report.utils.PedToPersonsParser;
import org.molgenis.vcf.report.utils.PersonListMerger;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Individual;
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

  private Items<Phenopacket> createPhenopackets(String phenotypes, List<Person> persons) {
    // FIXME implement
    List<SamplePhenotype> phenotypeList = parse(phenotypes);
    // per person phenotype?
    // yes: add per person
    // no: foreach person add phenotypes if affected
    return new Items(
        Arrays.asList(
            Phenopacket.newBuilder()
                .setId("test")
                .setSubject(Individual.newBuilder().setId("Individual").build())
                .build()),
        -1);
  }

  private List<SamplePhenotype> parse(String phenotypesString) {
    // FIXME: incorrect impl
    /*    List<SamplePhenotype> phenotypes = new ArrayList<>();
    String[] phenotypesArray = phenotypesString.split(",");
      PhenotypeMode mode = null;
      for(String phenotypeString : phenotypesArray){
        if (phenotypeString.contains("/")){
          if(mode != null && mode != PhenotypeMode.PER_SAMPLE_STRING){
            throw new RuntimeException("FIXME: combined string not allowed");
          }
          mode = PhenotypeMode.PER_SAMPLE_STRING;
          String[] samplePhenotype = phenotypeString.split("/", -1);
          String sample = samplePhenotype[0];
          phenotypes.add(new SamplePhenotype(mode,sample,parsePhenotypes(samplePhenotype[1])));
        }else{
          if(mode != null && mode != PhenotypeMode.STRING){
            throw new RuntimeException("FIXME: combined string not allowed");
          }
          mode = PhenotypeMode.STRING;
          phenotypes.add(new SamplePhenotype(mode,null,parsePhenotypes(phenotypesString)));
        }
      }
      return phenotypes;*/
    return Collections.emptyList();
  }

  private List<Phenotype> parsePhenotypes(String s) {
    return null;
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
