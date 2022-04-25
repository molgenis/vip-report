package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.molgenis.vcf.report.bam.BamSlice;
import org.molgenis.vcf.report.bam.VcfBamSlicerFactory;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.FastaSlice;
import org.molgenis.vcf.report.fasta.VcfFastaSlicer;
import org.molgenis.vcf.report.fasta.VcfFastaSlicerFactory;
import org.molgenis.vcf.report.genes.GenesFilter;
import org.molgenis.vcf.report.genes.GenesFilterFactory;
import org.molgenis.vcf.report.mapper.HtsFileMapper;
import org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper;
import org.molgenis.vcf.report.mapper.PedToSamplesMapper;
import org.molgenis.vcf.report.mapper.PhenopacketMapper;
import org.molgenis.vcf.report.model.Binary;
import org.molgenis.vcf.report.model.Bytes;
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
  private final VcfFastaSlicerFactory vcfFastaSlicerFactory;
  private final GenesFilterFactory genesFilterFactory;
  private final VcfBamSlicerFactory vcfBamSlicerFactory;

  public ReportGenerator(
      HtsJdkToPersonsMapper htsJdkToPersonsMapper,
      PhenopacketMapper phenopacketMapper,
      PedToSamplesMapper pedToSamplesMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper,
      VcfFastaSlicerFactory vcfFastaSlicerFactory,
      GenesFilterFactory genesFilterFactory,
      VcfBamSlicerFactory vcfBamSlicerFactory) {
    this.htsJdkToPersonsMapper = requireNonNull(htsJdkToPersonsMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.pedToSamplesMapper = requireNonNull(pedToSamplesMapper);
    this.personListMerger = requireNonNull(personListMerger);
    this.htsFileMapper = requireNonNull(htsFileMapper);
    this.vcfFastaSlicerFactory = requireNonNull(vcfFastaSlicerFactory);
    this.genesFilterFactory = requireNonNull(genesFilterFactory);
    this.vcfBamSlicerFactory = requireNonNull(vcfBamSlicerFactory);
  }

  public Report generateReport(
      Path inputVcfPath,
      SampleSettings sampleSettings,
      ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      report = createReport(vcfFileReader, inputVcfPath, sampleSettings, reportGeneratorSettings);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
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
      ReportGeneratorSettings reportGeneratorSettings)
      throws IOException {
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
    ReportData reportData = new ReportData(samples.getItems(), phenopackets);

    Map<String, Bytes> fastaGzMap;
    Path referencePath = reportGeneratorSettings.getReferencePath();
    if (referencePath != null) {
      VcfFastaSlicer vcfFastaSlicer = vcfFastaSlicerFactory.create(referencePath);
      List<FastaSlice> fastaGzSlices = vcfFastaSlicer.generate(vcfFileReader, 250);
      fastaGzMap = new LinkedHashMap<>();
      fastaGzSlices.forEach(
          fastaSlice -> {
            String key = getFastaSliceIdentifier(fastaSlice);
            fastaGzMap.put(key, new Bytes(fastaSlice.getFastaGz()));
          });
    } else {
      fastaGzMap = null;
    }

    Path genesPath = reportGeneratorSettings.getGenesPath();
    Bytes genesGz;
    if (genesPath != null) {
      GenesFilter genesFilter = genesFilterFactory.create(genesPath);
      genesGz = new Bytes(genesFilter.filter(vcfFileReader, 250));
    } else {
      genesGz = null;
    }

    Map<String, Bytes> bamMap = new LinkedHashMap<>();
    sampleSettings
        .getBamPaths()
        .forEach(
            (sampleId, bamPath) -> {
              BamSlice bamSlice =
                  vcfBamSlicerFactory.create(bamPath).generate(vcfFileReader, 250, sampleId);
              bamMap.put(sampleId, new Bytes(bamSlice.getBam()));
            });

    Bytes vcfBytes;
    if (vcfPath.toString().endsWith(".gz")) {
      try (GZIPInputStream inputStream = new GZIPInputStream(Files.newInputStream(vcfPath))) {
        vcfBytes = new Bytes(inputStream.readAllBytes());
      }
    } else {
      vcfBytes = new Bytes(Files.readAllBytes(vcfPath));
    }

    Path decisionTreePath = reportGeneratorSettings.getDecisionTreePath();
    Map<?,?> decisionTree;
    if (decisionTreePath != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        decisionTree = mapper.readValue(decisionTreePath.toFile(), Map.class);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else {
      decisionTree = null;
    }

    Binary binary = new Binary(vcfBytes, fastaGzMap, genesGz, bamMap);
    return new Report(reportMetadata, reportData, binary, decisionTree);
  }

  private static String getFastaSliceIdentifier(FastaSlice fastaSlice) {
    ContigInterval interval = fastaSlice.getInterval();
    return interval.getContig() + ':' + interval.getStart() + '-' + interval.getStop();
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
