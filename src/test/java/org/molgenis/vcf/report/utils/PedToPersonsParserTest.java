package org.molgenis.vcf.report.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.utils.PedIndividual.AffectionStatus;
import org.molgenis.vcf.report.utils.PedIndividual.Sex;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;

@ExtendWith(MockitoExtension.class)
class PedToPersonsParserTest {
  @Mock
  private PedReader pedReader;

  @Test
  void parse() {
    PedIndividual individual1 = new PedIndividual("fam1","id1","paternal1","maternal1", Sex.MALE, AffectionStatus.AFFECTED);
    PedIndividual individual2 = new PedIndividual("fam1","id2","paternal2","maternal2", Sex.FEMALE, AffectionStatus.UNAFFECTED);

    List<PedIndividual> pedIndividuals = Arrays.asList(individual1, individual2);
    when(pedReader.iterator()).thenReturn(pedIndividuals.iterator());

    Map<String, Person> expected = new HashMap<>();
    expected.put("id1", Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal1").setPaternalId("paternal1").setSex(
        org.phenopackets.schema.v1.core.Sex.MALE).setAffectedStatus(AffectedStatus.AFFECTED).build());
    expected.put("id2", Person.newBuilder().setIndividualId("id2").setFamilyId("fam1").setMaternalId("maternal2").setPaternalId("paternal2").setSex(
        org.phenopackets.schema.v1.core.Sex.FEMALE).setAffectedStatus(AffectedStatus.UNAFFECTED).build());

    assertEquals(expected,PedToPersonsParser.parse(pedReader, 10));
  }

  @Test
  void parseUnknown() {
    PedIndividual individual = new PedIndividual("fam1","id1","paternal","maternal", Sex.UNKNOWN, AffectionStatus.UNKNOWN);
    List<PedIndividual> pedIndividuals = Arrays.asList(individual);
    when(pedReader.iterator()).thenReturn(pedIndividuals.iterator());

    Map<String, Person> expected = new HashMap<>();
    expected.put("id1", Person.newBuilder().setIndividualId("id1").setFamilyId("fam1").setMaternalId("maternal").setPaternalId("paternal").setSex(
        org.phenopackets.schema.v1.core.Sex.UNKNOWN_SEX).setAffectedStatus(AffectedStatus.MISSING).build());

    assertEquals(expected,PedToPersonsParser.parse(pedReader, 10));
  }
}