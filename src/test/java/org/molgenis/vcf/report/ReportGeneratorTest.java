package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.utils.model.metadata.HtsFormat.VCF;

import com.fasterxml.jackson.databind.ObjectMapper;
import htsjdk.variant.vcf.VCFHeader;
import java.io.IOException;
import java.nio.file.Files;
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
import org.molgenis.vcf.report.bedmethyl.BedmethylFilterFactory;
import org.molgenis.vcf.report.fasta.*;
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.genes.GenesFilterFactory;
import org.molgenis.vcf.report.model.Binary;
import org.molgenis.vcf.report.model.Bytes;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.utils.PersonListMerger;
import org.molgenis.vcf.utils.model.metadata.HtsFile;
import org.molgenis.vcf.utils.sample.mapper.HtsFileMapper;
import org.molgenis.vcf.utils.sample.mapper.HtsJdkToPersonsMapper;
import org.molgenis.vcf.utils.sample.mapper.PhenopacketMapper;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

  @Mock private HtsJdkToPersonsMapper htsJdkToPersonsMapper;
  @Mock private PhenopacketMapper phenopacketMapper;
  @Mock private PersonListMerger personListMerger;
  @Mock private HtsFileMapper htsFileMapper;
  @Mock private VcfFastaSlicerFactory vcfFastaSlicerFactory;
  @Mock private GenesFilterFactory genesFilterFactory;
  @Mock private BedmethylFilterFactory bedmethylFilterFactory;
  private ReportGenerator reportGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    reportGenerator =
        new ReportGenerator(
            htsJdkToPersonsMapper,
            phenopacketMapper,
            personListMerger,
            htsFileMapper,
            vcfFastaSlicerFactory,
            genesFilterFactory,
            bedmethylFilterFactory);
  }

  @Test
  void generateReport() throws IOException {
    int maxNrSamples = 10;

    List<Sample> vcfSampleItems = emptyList();
    when(htsJdkToPersonsMapper.map(any(VCFHeader.class), eq(maxNrSamples)))
        .thenReturn(vcfSampleItems);

    List<Phenopacket> phenopacketList = emptyList();
    List<Phenopacket> phenopacketItems = phenopacketList;
    when(phenopacketMapper.mapPhenotypes(any(), any())).thenReturn(phenopacketItems);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    Path treePath = Paths.get("src", "test", "resources", "tree.json");
    List<Path> pedPath =
        Collections.singletonList(Paths.get("src", "test", "resources", "example.ped"));
    Path referencePath = Paths.get("src", "test", "resources", "example.fasta.gz");

    Map<String, Sample> pedSampleItems =
        Map.of("John", Sample.builder().person(Person.builder().familyId("FAM001").sex(Sex.MALE).affectedStatus(
            AffectedStatus.AFFECTED).maternalId("Jane").individualId("John").paternalId("Jimmy").build()).proband(false).index(-1).build(),
            "James", Sample.builder().person(Person.builder().familyId("FAM002").sex(Sex.MALE).affectedStatus(
            AffectedStatus.UNAFFECTED).maternalId("0").individualId("James").paternalId("0").build()).proband(false).index(-1).build(),
            "Jane", Sample.builder().person(Person.builder().familyId("FAM001").sex(Sex.FEMALE).affectedStatus(
            AffectedStatus.UNAFFECTED).maternalId("0").individualId("Jane").paternalId("0").build()).proband(false).index(-1).build(),
            "Jimmy", Sample.builder().person(Person.builder().familyId("FAM001").sex(Sex.MALE).affectedStatus(
            AffectedStatus.UNAFFECTED).maternalId("0").individualId("Jimmy").paternalId("0").build()).proband(false).index(-1).build());

    List<Sample> sampleList = emptyList();
    when(personListMerger.merge(vcfSampleItems, pedSampleItems, 10))
        .thenReturn(sampleList);

    HtsFile htsFile = new HtsFile("test.vcf", VCF, "GRCh38");
    when(htsFileMapper.map(any(), eq(inputVcfPath.toString()))).thenReturn(htsFile);

    VariantFastaSlicer variantFastaSlicer = mock(VariantFastaSlicer.class);
    Map<String, Bytes> fastaMap = Map.of("1:2-3", new Bytes(new byte[] {0}));
    when(variantFastaSlicer.generate(any(), any(), any())).thenReturn(fastaMap);
    when(vcfFastaSlicerFactory.create(referencePath)).thenReturn(variantFastaSlicer);

    String phenotypes = "hpo:123456;omim3456";
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArgs = "MyArgs";
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(
            appName, appVersion, appArgs, maxNrSamples, referencePath, null, treePath);
    Report report =
        new Report(
            new ReportMetadata(new AppMetadata(appName, appVersion, appArgs), htsFile),
            new ReportData(sampleList, phenopacketList),
            new Binary(
                new Bytes(Files.readAllBytes(inputVcfPath)),
                Map.of("1:2-3", new Bytes(new byte[] {0})),
                null,
                Map.of(),
                Map.of()),
            new ObjectMapper()
                .readValue(
                    "{\"name\":\"testtree\", \"description\":\"no need for a valid tree\"}",
                    Map.class));

    assertEquals(
        report,
        reportGenerator.generateReport(
            inputVcfPath,
            new SampleSettings(emptyList(), pedPath, phenotypes, Map.of(), Map.of()),
            reportGeneratorSettings));
  }
}
