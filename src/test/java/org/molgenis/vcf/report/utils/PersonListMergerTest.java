package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Sample;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;
import org.phenopackets.schema.v1.core.Sex;

@ExtendWith(MockitoExtension.class)
class PersonListMergerTest {

  @Mock
  private PersonListMerger personListMerger;

  @BeforeEach
  void setUpBeforeEach(){
    personListMerger = new PersonListMerger();
  }

  @Test
  void merge() {
    Map<String, Sample> pedigreePersons = new HashMap<>();
    pedigreePersons.put("id1", new Sample(Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build(),-1));
    pedigreePersons.put("id2", new Sample(Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build(),-1));

    List<Sample> vcfPersons = new ArrayList<>();
    vcfPersons.add(new Sample(Person.newBuilder().setIndividualId("id1").setFamilyId("MISSING_0").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build(),0));
    vcfPersons.add(new Sample(Person.newBuilder().setIndividualId("id3").setFamilyId("MISSING_1").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build(),1));

    List<Sample> expected = new ArrayList<>();
    expected.add(new Sample(Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build(),0));
    expected.add(new Sample(Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build(),-1));
    expected.add(new Sample(Person.newBuilder().setIndividualId("id3").setFamilyId("MISSING_1").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build(),1));

    Items<Sample> actual = personListMerger.merge(vcfPersons, pedigreePersons, 10);
    assertTrue(actual.getItems().containsAll(expected));
    assertEquals(expected.size(),actual.getItems().size());
    assertEquals(3, actual.getTotal());
  }
}