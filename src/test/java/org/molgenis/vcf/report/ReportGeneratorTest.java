package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.report.model.metadata.HtsFormat.VCF;

import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.bam.VcfBamSlicerFactory;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.FastaSlice;
import org.molgenis.vcf.report.fasta.VcfFastaSlicer;
import org.molgenis.vcf.report.fasta.VcfFastaSlicerFactory;
import org.molgenis.vcf.report.generator.Base85Encoder;
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.genes.GenesFilterFactory;
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

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

  @Mock private HtsJdkToPersonsMapper htsJdkToPersonsMapper;
  @Mock private PhenopacketMapper phenopacketMapper;
  @Mock private PedToSamplesMapper pedToSamplesMapper;
  @Mock private PersonListMerger personListMerger;
  @Mock private HtsFileMapper htsFileMapper;
  @Mock private Base85Encoder base85Encoder;
  @Mock private VcfFastaSlicerFactory vcfFastaSlicerFactory;
  @Mock private GenesFilterFactory genesFilterFactory;
  @Mock private VcfBamSlicerFactory vcfBamSlicerFactory;
  private ReportGenerator reportGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    reportGenerator =
        new ReportGenerator(
            htsJdkToPersonsMapper,
            phenopacketMapper,
            pedToSamplesMapper,
            personListMerger,
            htsFileMapper,
            base85Encoder,
            vcfFastaSlicerFactory,
            genesFilterFactory,
            vcfBamSlicerFactory);
  }

  @Test
  void generateReport() {
    int maxNrSamples = 10;
    int maxNrRecords = 100;

    Items<Sample> vcfSampleItems = new Items<>(emptyList(), 3);
    when(htsJdkToPersonsMapper.map(any(VCFHeader.class), eq(maxNrSamples)))
        .thenReturn(vcfSampleItems);

    Items<Phenopacket> phenopacketItems = new Items<>(emptyList(), 5);
    when(phenopacketMapper.mapPhenotypes(any(), any())).thenReturn(phenopacketItems);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    List<Path> pedPath =
        Collections.singletonList(Paths.get("src", "test", "resources", "example.ped"));
    Path referencePath = Paths.get("src", "test", "resources", "example.fasta.gz");

    Map<String, Sample> pedSampleItems = emptyMap();
    when(pedToSamplesMapper.mapPedFileToPersons(pedPath, 10)).thenReturn(pedSampleItems);

    Items<Sample> sampleItems = new Items<>(emptyList(), 6);
    when(personListMerger.merge(vcfSampleItems.getItems(), pedSampleItems, 10))
        .thenReturn(sampleItems);

    HtsFile htsFile = new HtsFile("test.vcf", VCF, "GRCh38");
    when(htsFileMapper.map(any(), eq(inputVcfPath.toString()))).thenReturn(htsFile);

    VcfFastaSlicer vcfFastaSlicer = mock(VcfFastaSlicer.class);
    FastaSlice fastaSlice = new FastaSlice(new ContigInterval("1", 2, 3), new byte[] {0});
    when(vcfFastaSlicer.generate(any(), eq(250))).thenReturn(List.of(fastaSlice));
    when(vcfFastaSlicerFactory.create(referencePath)).thenReturn(vcfFastaSlicer);

    String vcfGzBase85 = "vcfGzBase85";
    doReturn(vcfGzBase85).when(base85Encoder).encode(inputVcfPath);

    String phenotypes = "hpo:123456;omim3456";
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArgs = "MyArgs";
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName, appVersion, appArgs, maxNrSamples, maxNrRecords, referencePath, null);
    Report report =
        new Report(
            new ReportMetadata(new AppMetadata(appName, appVersion, appArgs), htsFile),
            new ReportData(sampleItems, phenopacketItems),
            new Base85(vcfGzBase85, Map.of("1:2-3", "00"), null, Map.of()));

    assertEquals(
        report,
        reportGenerator.generateReport(
            inputVcfPath,
            new SampleSettings(emptyList(), pedPath, phenotypes, Map.of()),
            reportGeneratorSettings));
  }
}
