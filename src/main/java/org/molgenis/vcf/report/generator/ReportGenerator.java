package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.utils.PathUtils.getDatabaseLocation;
import static org.molgenis.vcf.utils.sample.mapper.PedToSamplesMapper.mapPedFileToPersons;

import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFIterator;
import htsjdk.variant.vcf.VCFIteratorBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VariantFastaSlicer;
import org.molgenis.vcf.report.fasta.VariantIntervalCalculator;
import org.molgenis.vcf.report.fasta.VcfFastaSlicerFactory;
import org.molgenis.vcf.report.genes.GenesFilter;
import org.molgenis.vcf.report.genes.GenesFilterFactory;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.report.repository.DatabaseManager;
import org.molgenis.vcf.report.repository.DatabaseSchemaManager;
import org.molgenis.vcf.report.utils.VcfInputStreamDecorator;
import org.molgenis.vcf.utils.PersonListMerger;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.FieldMetadataServiceImpl;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
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
  private final VcfFastaSlicerFactory vcfFastaSlicerFactory;
  private final VariantIntervalCalculator variantIntervalCalculator;
  private final DatabaseSchemaManager databaseSchemaManager;
  private final DatabaseManager databaseManager;

  public ReportGenerator(
      HtsJdkToPersonsMapper htsJdkToPersonsMapper,
      PhenopacketMapper phenopacketMapper,
      PersonListMerger personListMerger,
      HtsFileMapper htsFileMapper,
      VcfFastaSlicerFactory vcfFastaSlicerFactory,
      GenesFilterFactory genesFilterFactory,
      VariantIntervalCalculator variantIntervalCalculator,
      DatabaseSchemaManager databaseSchemaManager,
      DatabaseManager databaseManager) {
    this.htsJdkToPersonsMapper = requireNonNull(htsJdkToPersonsMapper);
    this.phenopacketMapper = requireNonNull(phenopacketMapper);
    this.personListMerger = requireNonNull(personListMerger);
    this.htsFileMapper = requireNonNull(htsFileMapper);
    this.genesFilterFactory = requireNonNull(genesFilterFactory);
    this.vcfFastaSlicerFactory = requireNonNull(vcfFastaSlicerFactory);
    this.variantIntervalCalculator = requireNonNull(variantIntervalCalculator);
    this.databaseManager = requireNonNull(databaseManager);
    this.databaseSchemaManager = requireNonNull(databaseSchemaManager);
  }

  public Report generateReport(
      Path inputVcfPath,
      SampleSettings sampleSettings,
      ReportGeneratorSettings reportGeneratorSettings) {
    Report report;
    try (VCFIterator vcfIterator = createReader(inputVcfPath)) {
      report = createReport(vcfIterator, inputVcfPath, sampleSettings, reportGeneratorSettings);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return report;
  }

  private VCFIterator createReader(Path vcfPath) throws IOException {
    return new VCFIteratorBuilder().open(VcfInputStreamDecorator.preprocessVCF(vcfPath.toFile()));
  }

  private Report createReport(
      VCFIterator vcfFileReader,
      Path vcfPath,
      SampleSettings sampleSettings,
      ReportGeneratorSettings reportGeneratorSettings)
      throws IOException {
    HtsFile htsFile = htsFileMapper.map(vcfFileReader.getHeader(), vcfPath.toString());

    Items<Sample> samples =
        createPersons(
            vcfFileReader,
            sampleSettings.getProbandNames(),
            sampleSettings.getPedigreePaths(),
            reportGeneratorSettings);

    Items<Phenopacket> phenopackets = createPhenopackets(sampleSettings, samples);

    AppMetadata appMetadata =
        new AppMetadata(
            reportGeneratorSettings.getAppName(),
            reportGeneratorSettings.getAppVersion(),
            reportGeneratorSettings.getAppArguments());
    ReportMetadata reportMetadata = new ReportMetadata(appMetadata, htsFile);

    Map<String, Bytes> fastaGzMap;
    Path referencePath = reportGeneratorSettings.getReferencePath();
    Map<String, SampleSettings.CramPath> cramPaths = sampleSettings.getCramPaths();
    List<ContigInterval> contigIntervals =
        variantIntervalCalculator.calculate(vcfFileReader, cramPaths, referencePath);
    fastaGzMap = getReferenceTrackData(contigIntervals, referencePath);
    Bytes genesGz = getGenesTrackData(contigIntervals, reportGeneratorSettings);
    Map<String, Report.Cram> cramMap = getAlignmentTrackData(sampleSettings);

    Map<?, ?> templateConfig = parseJsonObject(reportGeneratorSettings.getTemplateConfigPath());
    String databaseLocation = getDatabaseLocation(vcfPath);
    databaseSchemaManager.createDatabase(
        reportGeneratorSettings,
        vcfFileReader.getHeader(),
        databaseManager.getConnection(databaseLocation));
    FieldMetadataService fieldMetadataService =
        new FieldMetadataServiceImpl(reportGeneratorSettings.getMetadataPath().toFile());
    FieldMetadatas fieldMetadatas = fieldMetadataService.load(vcfFileReader.getHeader());
    Bytes database;
    try {
      database =
          databaseManager.populateDb(
              databaseLocation,
              fieldMetadatas,
              samples,
              vcfPath.toFile(),
              reportGeneratorSettings.getDecisionTreePath(),
              reportGeneratorSettings.getSampleTreePath(),
              reportMetadata,
              templateConfig,
              phenopackets.getItems());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Bytes sqlWasm = new Bytes(Files.readAllBytes(reportGeneratorSettings.getSqlWasmPath()));
    return new Report(fastaGzMap, genesGz, cramMap, sqlWasm, database);
  }

  private static Map<?, ?> parseJsonObject(Path jsonPath) {
    Map<?, ?> jsonObject;
    if (jsonPath != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        jsonObject = mapper.readValue(jsonPath.toFile(), Map.class);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else {
      jsonObject = null;
    }
    return jsonObject;
  }

  private Items<Phenopacket> createPhenopackets(
      SampleSettings sampleSettings, Items<Sample> samples) {
    Items<Phenopacket> phenopackets;
    String phenotypes = sampleSettings.getPhenotypeString();
    if (phenotypes != null && !phenotypes.isEmpty()) {
      List<Phenopacket> phenopacketsList =
          phenopacketMapper.mapPhenotypes(phenotypes, samples.getItems());
      phenopackets = new Items<>(phenopacketsList, phenopacketsList.size());
    } else {
      phenopackets = new Items<>(Collections.emptyList(), 0);
    }
    return phenopackets;
  }

  private static Map<String, Report.Cram> getAlignmentTrackData(SampleSettings sampleSettings) {
    Map<String, Report.Cram> cramMap = new LinkedHashMap<>();
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
              cramMap.put(sampleId, new Report.Cram(new Bytes(cram), new Bytes(crai)));
            });
    return cramMap;
  }

  private Map<String, Bytes> getReferenceTrackData(
      List<ContigInterval> contigIntervals, Path referencePath) {
    Map<String, Bytes> fastaGzMap;
    if (referencePath != null) {
      VariantFastaSlicer variantFastaSlicer = vcfFastaSlicerFactory.create(referencePath);
      fastaGzMap = variantFastaSlicer.generate(contigIntervals, referencePath);
    } else {
      fastaGzMap = null;
    }
    return fastaGzMap;
  }

  private Bytes getGenesTrackData(
      List<ContigInterval> contigIntervals, ReportGeneratorSettings reportGeneratorSettings) {
    Path genesPath = reportGeneratorSettings.getGenesPath();
    Bytes genesGz;
    if (genesPath != null) {
      GenesFilter genesFilter = genesFilterFactory.create(genesPath);
      genesGz = new Bytes(genesFilter.filter(contigIntervals));
    } else {
      genesGz = null;
    }
    return genesGz;
  }

  private Items<Sample> createPersons(
      VCFIterator vcfFileReader,
      List<String> probandNames,
      List<Path> pedigreePaths,
      ReportGeneratorSettings settings) {
    VCFHeader fileHeader = vcfFileReader.getHeader();
    int maxNrSamples = settings.getMaxNrSamples();
    List<Sample> samplesList = htsJdkToPersonsMapper.map(fileHeader, maxNrSamples);
    Items<Sample> sampleItems = new Items<>(samplesList, samplesList.size());
    if (pedigreePaths != null) {
      final Map<String, Sample> pedigreePersons = mapPedFileToPersons(pedigreePaths, maxNrSamples);
      List<Sample> mergedSamples =
          personListMerger.merge(samplesList, pedigreePersons, maxNrSamples);
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
