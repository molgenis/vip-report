package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.Phenopacket.Builder;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

@ExtendWith(MockitoExtension.class)
class PhenopacketMapperTest {

  @Mock
  private PhenopacketMapper phenopacketMapper;

  @BeforeEach
  void setUpBeforeEach(){
    phenopacketMapper = new PhenopacketMapper();
  }

  @Test
  void mapPhenotypesGeneral() {
    List<Person> persons = new ArrayList<>();
    persons.add(Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build());
    persons.add(Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build());

    List<Phenopacket> expected = new ArrayList<>();

    expected.add(createPhenopacket("id1", Arrays.asList("HPO:123","headache","omim234")));

    Items<Phenopacket> actual = phenopacketMapper
        .mapPhenotypes("HPO:123;headache;omim234", persons);
    assertEquals(expected,actual.getItems());
    assertEquals(expected.size(),actual.getItems().size());
  }

  @Test
  void mapPhenotypesPerSample() {
    List<Phenopacket> expected = new ArrayList<>();

    expected.add(createPhenopacket("sample1", Collections.singletonList("HPO:123")));
    expected.add(createPhenopacket("sample2", Arrays.asList("headache","omim234")));

    Items<Phenopacket> actual = phenopacketMapper
        .mapPhenotypes("sample1/HPO:123,sample2/headache;omim234", Collections.emptyList());
    assertEquals(expected,actual.getItems());
    assertEquals(expected.size(),actual.getItems().size());
  }

  private Phenopacket createPhenopacket(String sampleId, List<String> phenotypes) {
    Builder builder = Phenopacket.newBuilder();
    builder.setSubject(Individual.newBuilder().setId(sampleId).build());
    for (String phenotype : phenotypes) {
      PhenotypicFeature phenotypicFeature =
          PhenotypicFeature.newBuilder()
              .addModifiers(OntologyClass.newBuilder().setId(phenotype).setLabel(phenotype).build())
              .build();
      builder.addPhenotypicFeatures(phenotypicFeature);
    }
    return builder.build();
  }
}