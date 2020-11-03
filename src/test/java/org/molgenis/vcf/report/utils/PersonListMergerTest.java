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
        Sample.builder()
            .person(
                new Person(
                    "fam1", "id1", "paternal1", "maternal1", Sex.MALE, AffectedStatus.AFFECTED))
            .index(-1)
            .build());
    pedigreePersons.put(
        "id2",
        Sample.builder()
            .person(
                new Person(
                    "fam1", "id2", "paternal2", "maternal2", Sex.FEMALE, AffectedStatus.UNAFFECTED))
            .index(-1)
            .build());

    List<Sample> vcfPersons = new ArrayList<>();
    vcfPersons.add(
        Sample.builder()
            .person(
                new Person(
                    "MISSING_0",
                    "id1",
                    MISSING_PERSON_ID,
                    MISSING_PERSON_ID,
                    Sex.UNKNOWN_SEX,
                    AffectedStatus.MISSING))
            .index(0)
            .build());
    vcfPersons.add(
        Sample.builder()
            .person(
                new Person(
                    "MISSING_1",
                    "id3",
                    MISSING_PERSON_ID,
                    MISSING_PERSON_ID,
                    Sex.UNKNOWN_SEX,
                    AffectedStatus.MISSING))
            .index(1)
            .build());

    List<Sample> expected = new ArrayList<>();
    expected.add(
        Sample.builder()
            .person(
                new Person(
                    "fam1", "id1", "paternal1", "maternal1", Sex.MALE, AffectedStatus.AFFECTED))
            .index(0)
            .build());
    expected.add(
        Sample.builder()
            .person(
                new Person(
                    "fam1", "id2", "paternal2", "maternal2", Sex.FEMALE, AffectedStatus.UNAFFECTED))
            .index(-1)
            .build());
    expected.add(
        Sample.builder()
            .person(
                new Person(
                    "MISSING_1",
                    "id3",
                    MISSING_PERSON_ID,
                    MISSING_PERSON_ID,
                    Sex.UNKNOWN_SEX,
                    AffectedStatus.MISSING))
            .index(1)
            .build());

    Items<Sample> actual = personListMerger.merge(vcfPersons, pedigreePersons, 10);
    assertAll(
        () -> assertTrue(actual.getItems().containsAll(expected)),
        () -> assertEquals(expected.size(), actual.getItems().size()),
        () -> assertEquals(3, actual.getTotal()));
  }
}
