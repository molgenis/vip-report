package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.AffectedStatus;
import org.molgenis.vcf.report.model.Individual;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.OntologyClass;
import org.molgenis.vcf.report.model.Person;
import org.molgenis.vcf.report.model.Phenopacket;
import org.molgenis.vcf.report.model.PhenotypicFeature;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.Sex;

@ExtendWith(MockitoExtension.class)
class PhenopacketMapperTest {

  @Mock private PhenopacketMapper phenopacketMapper;

  @BeforeEach
  void setUpBeforeEach() {
    phenopacketMapper = new PhenopacketMapper();
  }

  @Test
  void mapPhenotypesGeneral() {
    List<Sample> samples = new ArrayList<>();
    samples.add(
        new Sample(
            new Person("fam1", "id1", "maternal1", "paternal1", Sex.MALE, AffectedStatus.AFFECTED),
            -1));
    samples.add(
        new Sample(
            new Person(
                "fam1", "id2", "maternal2", "paternal2", Sex.FEMALE, AffectedStatus.UNAFFECTED),
            -1));

    List<Phenopacket> expected = new ArrayList<>();

    expected.add(createPhenopacket("id1", Arrays.asList("HP:123", "test:headache", "omim:234")));

    Items<Phenopacket> actual =
        phenopacketMapper.mapPhenotypes("HP:123;test:headache;omim:234", samples);
    assertAll(
        () -> assertEquals(expected, actual.getItems()),
        () -> assertEquals(expected.size(), actual.getItems().size()));
  }

  @Test
  void mapPhenotypesPerSample() {
    List<Phenopacket> expected = new ArrayList<>();

    expected.add(createPhenopacket("sample1", Collections.singletonList("HP:123")));
    expected.add(createPhenopacket("sample2", Arrays.asList("test:headache", "omim:234")));

    Items<Phenopacket> actual =
        phenopacketMapper.mapPhenotypes(
            "sample1/HP:123,sample2/test:headache;omim:234", Collections.emptyList());
    assertAll(
        () -> assertEquals(expected, actual.getItems()),
        () -> assertEquals(expected.size(), actual.getItems().size()));
  }

  @Test
  void mapInvalidPhenotypes() {
    List<Sample> samples = Collections.emptyList();
    assertThrows(
        IllegalPhenotypeArgumentException.class,
        () -> {
          phenopacketMapper.mapPhenotypes("sample1/HP:123,sample2/headache;omim:234", samples);
        });
  }

  private Phenopacket createPhenopacket(String sampleId, List<String> phenotypes) {
    @NonNull List<PhenotypicFeature> features = new ArrayList<>();
    for (String phenotype : phenotypes) {
      PhenotypicFeature phenotypicFeature =
          new PhenotypicFeature(new OntologyClass(phenotype, phenotype));
      features.add(phenotypicFeature);
    }
    return new Phenopacket(features, new Individual(sampleId));
  }
}
