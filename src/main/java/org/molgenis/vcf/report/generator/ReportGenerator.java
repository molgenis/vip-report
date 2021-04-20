package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.mapper.HtsFileMapper;
import org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper;
import org.molgenis.vcf.report.mapper.PedToSamplesMapper;
import org.molgenis.vcf.report.mapper.PhenopacketMapper;
import org.molgenis.vcf.report.model.Base85;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Phenopacket;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.HtsFile;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.report.utils.PersonListMerger;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {

  private final HtsJdkToPersonsMapper htsJdkToPersonsMapper;
  private final PhenopacketMapper phenopacketMapper;
  private final PedToSamplesMapper pedToSamplesMapper;
  private final PersonListMerger personListMerger;
  private final HtsFileMapper htsFileMapper;
  private final Base85Encoder base85Encoder;

  public ReportGenerator(
      HtsJdkToPersonsMapper htsJdkToPersonsMapper,
      PhenopacketMapper phenopacketMapper,
      PedToSamplesMapper pedToSamplesMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper,
      Base85Encoder base85Encoder) {
    this.htsJdkToPersonsMapper = requireNonNull(htsJdkToPersonsMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.pedToSamplesMapper = requireNonNull(pedToSamplesMapper);
    this.personListMerger = requireNonNull(personListMerger);
    this.htsFileMapper = requireNonNull(htsFileMapper);
    this.base85Encoder = requireNonNull(base85Encoder);
  }

  public Report generateReport(
      Path inputVcfPath,
      SampleSettings sampleSettings,
      ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      report = createReport(vcfFileReader, inputVcfPath, sampleSettings, reportGeneratorSettings);
    }
    return report;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }

  private Report createReport(
      VCFFileReader vcfFileReader,
      Path vcfPath,
      SampleSettings sampleSettings,
      ReportGeneratorSettings reportGeneratorSettings) {
    HtsFile htsFile = htsFileMapper.map(vcfFileReader.getFileHeader(), vcfPath.toString());

    Items<Sample> samples =
        createPersons(
            vcfFileReader,
            sampleSettings.getProbandNames(),
            sampleSettings.getPedigreePaths(),
            reportGeneratorSettings);

    Items<Phenopacket> phenopackets;
    String phenotypes = sampleSettings.getPhenotypeString();
    if (phenotypes != null && !phenotypes.isEmpty()) {
      phenopackets = phenopacketMapper.mapPhenotypes(phenotypes, samples.getItems());
    } else {
      phenopackets = new Items<>(Collections.emptyList(), 0);
    }

    AppMetadata appMetadata =
        new AppMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportMetadata reportMetadata = new ReportMetadata(appMetadata, htsFile);
    ReportData reportData = new ReportData(samples, phenopackets);
    Base85 base85 = new Base85(base85Encoder.encode(vcfPath));
    return new Report(reportMetadata, reportData, base85);
  }

  private Items<Sample> createPersons(
      VCFFileReader vcfFileReader,
      List<String> probandNames,
      List<Path> pedigreePaths,
      ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    Items<Sample> sampleItems = htsJdkToPersonsMapper.map(fileHeader, maxNrSamples);
    if (pedigreePaths != null) {
      final Map<String, Sample> pedigreePersons =
          pedToSamplesMapper.mapPedFileToPersons(pedigreePaths, maxNrSamples);
      sampleItems = personListMerger.merge(sampleItems.getItems(), pedigreePersons, maxNrSamples);
    }
    if (!probandNames.isEmpty()) {
      sampleItems
          .getItems()
          .forEach(
              sample -> {
                if (probandNames.contains(sample.getPerson().getIndividualId())) {
                  sample.setProband(true);
                }
              });
    } else {
      sampleItems.getItems().forEach(sample -> sample.setProband(true));
    }
    return sampleItems;
  }
}
