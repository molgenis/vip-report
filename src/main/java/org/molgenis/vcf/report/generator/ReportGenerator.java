package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.utils.sample.mapper.PedToSamplesMapper.mapPedFileToPersons;

import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.molgenis.vcf.report.bedmethyl.BedmethylFilter;
import org.molgenis.vcf.report.bedmethyl.BedmethylFilterFactory;
import org.molgenis.vcf.report.fasta.*;
import org.molgenis.vcf.report.genes.GenesFilter;
import org.molgenis.vcf.report.genes.GenesFilterFactory;
import org.molgenis.vcf.report.model.Binary;
import org.molgenis.vcf.report.model.Binary.Cram;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.PersonListMerger;
import org.molgenis.vcf.utils.model.metadata.HtsFile;
import org.molgenis.vcf.utils.sample.mapper.HtsFileMapper;
import org.molgenis.vcf.utils.sample.mapper.HtsJdkToPersonsMapper;
import org.molgenis.vcf.utils.sample.mapper.PhenopacketMapper;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {
  private final HtsJdkToPersonsMapper htsJdkToPersonsMapper;
  private final PhenopacketMapper phenopacketMapper;
  private final PersonListMerger personListMerger;
  private final HtsFileMapper htsFileMapper;
  private final GenesFilterFactory genesFilterFactory;
  private final BedmethylFilterFactory bedmethylFilterFactory;
  private final VcfFastaSlicerFactory vcfFastaSlicerFactory;

  public ReportGenerator(
      HtsJdkToPersonsMapper htsJdkToPersonsMapper,
      PhenopacketMapper phenopacketMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper,
      VcfFastaSlicerFactory vcfFastaSlicerFactory,
      GenesFilterFactory genesFilterFactory,
      BedmethylFilterFactory bedmethtylFilterFactory) {
    this.htsJdkToPersonsMapper = requireNonNull(htsJdkToPersonsMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.personListMerger = requireNonNull(personListMerger);
    this.htsFileMapper = requireNonNull(htsFileMapper);
    this.genesFilterFactory = requireNonNull(genesFilterFactory);
    this.bedmethylFilterFactory = requireNonNull(bedmethtylFilterFactory);
    this.vcfFastaSlicerFactory = requireNonNull(vcfFastaSlicerFactory);
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
      List<Phenopacket> phenopacketsList = phenopacketMapper.mapPhenotypes(
          phenotypes, samples.getItems());
      phenopackets = new Items<>(phenopacketsList, phenopacketsList.size());
    } else {
      phenopackets = new Items<>(Collections.emptyList(), 0);
    }

    AppMetadata appMetadata =
        new AppMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportMetadata reportMetadata = new ReportMetadata(appMetadata, htsFile);
    ReportData reportData = new ReportData(samples.getItems(), phenopackets.getItems());

    Map<String, Bytes> fastaGzMap;
    Path referencePath = reportGeneratorSettings.getReferencePath();
    Map<String, SampleSettings.CramPath> cramPaths = sampleSettings.getCramPaths();
    fastaGzMap = getReferenceTrackData(vcfFileReader, referencePath, cramPaths);
    Bytes genesGz = getGenesTrackData(vcfFileReader, reportGeneratorSettings, referencePath, cramPaths);
    Map<String, Cram> cramMap = getAlignmentTrackData(sampleSettings);
    Map<String, Bytes> bedmethylMap = getBedmethylTrackData(sampleSettings, vcfFileReader, referencePath, cramPaths);
    Bytes vcfBytes = getVariantTrackData(vcfPath);

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

    Binary binary = new Binary(vcfBytes, fastaGzMap, genesGz, cramMap, bedmethylMap);
    return new Report(reportMetadata, reportData, binary, decisionTree);
  }

  private static Bytes getVariantTrackData(Path vcfPath) throws IOException {
    Bytes vcfBytes;
    if (vcfPath.toString().endsWith(".gz")) {
      try (GZIPInputStream inputStream = new GZIPInputStream(Files.newInputStream(vcfPath))) {
        vcfBytes = new Bytes(inputStream.readAllBytes());
      }
    } else {
      vcfBytes = new Bytes(Files.readAllBytes(vcfPath));
    }
    return vcfBytes;
  }

  private static Map<String, Cram> getAlignmentTrackData(SampleSettings sampleSettings) {
    Map<String, Cram> cramMap = new LinkedHashMap<>();
    sampleSettings
        .getCramPaths()
        .forEach(
            (sampleId, cramPath) -> {
              byte[] cram;
              byte[] crai;
              try {
                cram = Files.readAllBytes(cramPath.getCram());
                crai = Files.readAllBytes(cramPath.getCrai());
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
              cramMap.put(sampleId, new Cram(new Bytes(cram),new Bytes(crai)));
            });
    return cramMap;
  }

  private Map<String, Bytes> getBedmethylTrackData(SampleSettings sampleSettings, VCFFileReader vcfFileReader, Path referencePath, Map<String, SampleSettings.CramPath> cramPaths) {
    Map<String, Bytes> bedmethylMap = new LinkedHashMap<>();
    sampleSettings
            .getBedmethylPaths()
            .forEach(
                    (sampleId, BedmethylPath) -> {
                      byte [] bedmethyl;
                        BedmethylFilter bedmethylFilter = bedmethylFilterFactory.create(BedmethylPath.getBedmethyl());
                        bedmethyl = bedmethylFilter.filter(vcfFileReader, cramPaths, referencePath);
                        bedmethylMap.put(sampleId, new Bytes(bedmethyl));
                    });
    return bedmethylMap;
  }

  private Map<String, Bytes> getReferenceTrackData(VCFFileReader vcfFileReader, Path referencePath, Map<String, SampleSettings.CramPath> cramPaths) {
    Map<String, Bytes> fastaGzMap;
    if (referencePath != null) {
      VariantFastaSlicer variantFastaSlicer = vcfFastaSlicerFactory.create(referencePath);
      fastaGzMap = variantFastaSlicer.generate(vcfFileReader, cramPaths, referencePath);
    } else {
      fastaGzMap = null;
    }
    return fastaGzMap;
  }

  private Bytes getGenesTrackData(VCFFileReader vcfFileReader, ReportGeneratorSettings reportGeneratorSettings, Path referencePath, Map<String, SampleSettings.CramPath> cramPaths) {
    Path genesPath = reportGeneratorSettings.getGenesPath();
    Bytes genesGz;
    if (genesPath != null) {
      GenesFilter genesFilter = genesFilterFactory.create(genesPath);
      genesGz = new Bytes(genesFilter.filter(vcfFileReader, cramPaths, referencePath));
    } else {
      genesGz = null;
    }
    return genesGz;
  }

  private Items<Sample> createPersons(
      VCFFileReader vcfFileReader,
      List<String> probandNames,
      List<Path> pedigreePaths,
      ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getFileHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    List<Sample> samplesList = htsJdkToPersonsMapper.map(fileHeader, maxNrSamples);
    Items<Sample> sampleItems = new Items<>(samplesList, samplesList.size());
    if (pedigreePaths != null) {
      final Map<String, Sample> pedigreePersons =
          mapPedFileToPersons(pedigreePaths, maxNrSamples);
      List<Sample> mergedSamples = personListMerger.merge(samplesList, pedigreePersons,
          maxNrSamples);
      sampleItems = new Items<>(mergedSamples, mergedSamples.size());
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
