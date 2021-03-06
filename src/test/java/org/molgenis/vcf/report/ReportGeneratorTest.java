package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.mapper.HtsFileMapper;
import org.molgenis.vcf.report.mapper.HtsJdkMapper;
import org.molgenis.vcf.report.mapper.PedToSamplesMapper;
import org.molgenis.vcf.report.mapper.PhenopacketMapper;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Phenopacket;
import org.molgenis.vcf.report.model.Record;
import org.molgenis.vcf.report.model.Report;
import org.molgenis.vcf.report.model.ReportData;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.metadata.AppMetadata;
import org.molgenis.vcf.report.model.metadata.HtsFile;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;
import org.molgenis.vcf.report.utils.PersonListMerger;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

  @Mock private HtsJdkMapper htsJdkMapper;
  @Mock private PhenopacketMapper phenopacketMapper;
  @Mock private PedToSamplesMapper pedToSamplesMapper;
  @Mock private PersonListMerger personListMerger;
  @Mock private HtsFileMapper htsFileMapper;
  private ReportGenerator reportGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    reportGenerator =
        new ReportGenerator(
            htsJdkMapper, phenopacketMapper, pedToSamplesMapper, personListMerger, htsFileMapper);
  }

  @Test
  void generateReport() {
    int maxNrSamples = 10;
    int maxNrRecords = 100;

    Items<Sample> vcfSampleItems = new Items<>(emptyList(), 3);
    when(htsJdkMapper.mapSamples(any(VCFHeader.class), eq(maxNrSamples)))
        .thenReturn(vcfSampleItems);

    RecordsMetadata recordsMetadata = mock(RecordsMetadata.class);
    when(htsJdkMapper.mapRecordsMetadata(any())).thenReturn(recordsMetadata);

    Items<Record> recordItems = new Items<>(emptyList(), 5);
    when(htsJdkMapper.mapRecords(any(), any(), eq(maxNrRecords), any())).thenReturn(recordItems);

    Items<Phenopacket> phenopacketItems = new Items<>(emptyList(), 5);
    when(phenopacketMapper.mapPhenotypes(any(), any())).thenReturn(phenopacketItems);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    List<Path> pedPath =
        Collections.singletonList(Paths.get("src", "test", "resources", "example.ped"));

    Map<String, Sample> pedSampleItems = emptyMap();
    when(pedToSamplesMapper.mapPedFileToPersons(pedPath, 10)).thenReturn(pedSampleItems);

    Items<Sample> sampleItems = new Items<>(emptyList(), 6);
    when(personListMerger.merge(vcfSampleItems.getItems(), pedSampleItems, 10))
        .thenReturn(sampleItems);

    HtsFile htsFile = new HtsFile("test.vcf", VCF, "GRCh38");
    when(htsFileMapper.map(any(), eq(inputVcfPath.toString()))).thenReturn(htsFile);

    String phenotypes = "hpo:123456;omim3456";
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArgs = "MyArgs";
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(appName, appVersion, appArgs, maxNrSamples, maxNrRecords);
    Report report =
        new Report(
            new ReportMetadata(
                new AppMetadata(appName, appVersion, appArgs), htsFile, recordsMetadata),
            new ReportData(sampleItems, phenopacketItems, recordItems));

    assertEquals(
        report,
        reportGenerator
            .generateReport(inputVcfPath, new SampleSettings(emptyList(), pedPath, phenotypes),
                reportGeneratorSettings));
  }
}
