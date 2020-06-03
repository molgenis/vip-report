package org.molgenis.vcf.report;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.generator.ReportGenerator;
import org.molgenis.vcf.report.generator.ReportGeneratorSettings;
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
import org.phenopackets.schema.v1.core.Pedigree.Person;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

  @Mock private HtsJdkMapper htsJdkMapper;
  @Mock private PhenopacketMapper phenopacketMapper;
  @Mock private PedToPersonMapper pedToPersonMapper;
  @Mock private PersonListMerger personListMerger;
  private ReportGenerator reportGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    reportGenerator = new ReportGenerator(htsJdkMapper, phenopacketMapper, pedToPersonMapper, personListMerger);
  }

  @Test
  void generateReport() {
    int maxNrSamples = 10;
    int maxNrRecords = 100;

    Items<Person> vcfSampleItems = new Items<>(emptyList(), 3);
    when(htsJdkMapper.mapSamples(any(VCFHeader.class), eq(maxNrSamples))).thenReturn(vcfSampleItems);

    Items<Record> recordItems = new Items<>(emptyList(), 5);
    when(htsJdkMapper.mapRecords(any(), eq(maxNrRecords), any())).thenReturn(recordItems);

    Items<Phenopacket> phenopacketItems = new Items<>(emptyList(), 5);
    when(phenopacketMapper.mapPhenotypes(any(),any())).thenReturn(phenopacketItems);

    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    List<Path> pedPath = Collections.singletonList(Paths.get("src", "test", "resources", "example.ped"));

    Map<String, Person> pedSampleItems = emptyMap();
    when(pedToPersonMapper.mapPedFileToPersons(pedPath, 10)).thenReturn(pedSampleItems);

    Items<Person> sampleItems = new Items<>(emptyList(), 6);
    when(personListMerger.merge(vcfSampleItems.getItems(), pedSampleItems, 10)).thenReturn(sampleItems);

    String phenotypes = "hpo:123456;omim3456";
    String appName = "MyApp";
    String appVersion = "MyVersion";
    String appArgs = "MyArgs";
    ReportGeneratorSettings reportGeneratorSettings =
        new ReportGeneratorSettings(appName, appVersion, appArgs, maxNrSamples, maxNrRecords);
    Report report =
        new Report(
            new ReportMetadata(appName, appVersion, appArgs),
            new ReportData(sampleItems, phenopacketItems, recordItems));
    assertEquals(report, reportGenerator.generateReport(inputVcfPath, pedPath, phenotypes, reportGeneratorSettings));
  }
}
