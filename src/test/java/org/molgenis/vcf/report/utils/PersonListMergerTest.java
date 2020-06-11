package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.vcf.report.mapper.HtsJdkToPersonsMapper.MISSING_PERSON_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.AffectedStatus;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Person;
import org.molgenis.vcf.report.model.Sample;
import org.molgenis.vcf.report.model.Sex;

@ExtendWith(MockitoExtension.class)
class PersonListMergerTest {

  @Mock private PersonListMerger personListMerger;

  @BeforeEach
  void setUpBeforeEach() {
    personListMerger = new PersonListMerger();
  }

  @Test
  void merge() {
    Map<String, Sample> pedigreePersons = new HashMap<>();
    pedigreePersons.put(
        "id1",
        new Sample(
            new Person("fam1", "id1", "maternal1", "paternal1", Sex.MALE, AffectedStatus.AFFECTED),
            -1));
    pedigreePersons.put(
        "id2",
        new Sample(
            new Person(
                "fam1", "id2", "maternal2", "paternal2", Sex.FEMALE, AffectedStatus.UNAFFECTED),
            -1));

    List<Sample> vcfPersons = new ArrayList<>();
    vcfPersons.add(
        new Sample(
            new Person(
                "MISSING_0",
                "id1",
                MISSING_PERSON_ID,
                MISSING_PERSON_ID,
                Sex.UNKNOWN_SEX,
                AffectedStatus.MISSING),
            0));
    vcfPersons.add(
        new Sample(
            new Person(
                "MISSING_1",
                "id3",
                MISSING_PERSON_ID,
                MISSING_PERSON_ID,
                Sex.UNKNOWN_SEX,
                AffectedStatus.MISSING),
            1));

    List<Sample> expected = new ArrayList<>();
    expected.add(
        new Sample(
            new Person("fam1", "id1", "maternal1", "paternal1", Sex.MALE, AffectedStatus.AFFECTED),
            0));
    expected.add(
        new Sample(
            new Person(
                "fam1", "id2", "maternal2", "paternal2", Sex.FEMALE, AffectedStatus.UNAFFECTED),
            -1));
    expected.add(
        new Sample(
            new Person(
                "MISSING_1",
                "id3",
                MISSING_PERSON_ID,
                MISSING_PERSON_ID,
                Sex.UNKNOWN_SEX,
                AffectedStatus.MISSING),
            1));

    Items<Sample> actual = personListMerger.merge(vcfPersons, pedigreePersons, 10);
    assertAll(
        () -> assertTrue(actual.getItems().containsAll(expected)),
        () -> assertEquals(expected.size(), actual.getItems().size()),
        () -> assertEquals(3, actual.getTotal()));
  }
}
