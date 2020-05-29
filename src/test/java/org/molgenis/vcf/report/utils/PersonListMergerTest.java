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
    Map<String, Person> pedigreePersons = new HashMap<>();
    pedigreePersons.put("id1", Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build());
    pedigreePersons.put("id2", Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build());

    List<Person> vcfPersons = new ArrayList<>();
    vcfPersons.add(Person.newBuilder().setIndividualId("id1").setFamilyId("MISSING_0").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build());
    vcfPersons.add(Person.newBuilder().setIndividualId("id3").setFamilyId("MISSING_1").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build());

    List<Person> expected = new ArrayList<>();
    expected.add(Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build());
    expected.add(Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build());
    expected.add(Person.newBuilder().setIndividualId("id3").setFamilyId("MISSING_1").setSex(
        Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build());

    Items<Person> actual = personListMerger.merge(vcfPersons, pedigreePersons, 10);
    assertTrue(actual.getItems().containsAll(expected));
    assertEquals(expected.size(),actual.getItems().size());
    assertEquals(3, actual.getTotal());
  }
}