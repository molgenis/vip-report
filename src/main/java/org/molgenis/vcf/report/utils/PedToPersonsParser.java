package org.molgenis.vcf.report.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.molgenis.vcf.report.utils.PedIndividual.AffectionStatus;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;
import org.phenopackets.schema.v1.core.Sex;

public class PedToPersonsParser {
  public static Map<String, Person> parse(PedReader reader, int maxNrSamples) {
    final Map<String, Person> pedigreePersons = new HashMap<>();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.iterator(), 0), false)
        .limit(maxNrSamples)
        .map(pedIndividual -> map(pedIndividual))
        .forEach(person -> pedigreePersons.put(person.getIndividualId(), person));
    return pedigreePersons;
  }

  private static Person map(PedIndividual pedIndividual) {
    return Person.newBuilder()
        .setFamilyId(pedIndividual.getFamilyId())
        .setIndividualId(pedIndividual.getId())
        .setMaternalId(pedIndividual.getMaternalId())
        .setPaternalId(pedIndividual.getPaternalId())
        .setSex(map(pedIndividual.getSex()))
        .setAffectedStatus(map(pedIndividual.getAffectionStatus()))
        .build();
  }

  private static Sex map(PedIndividual.Sex sex) {
    switch(sex){
      case MALE:
        return Sex.MALE;
      case FEMALE:
        return Sex.FEMALE;
      case UNKNOWN:
        return Sex.UNKNOWN_SEX;
      default:
        return Sex.OTHER_SEX;
    }
  }

  private static AffectedStatus map(AffectionStatus affectionStatus) {
    switch (affectionStatus){
      case AFFECTED:
        return AffectedStatus.AFFECTED;
      case UNAFFECTED:
        return AffectedStatus.UNAFFECTED;
      case UNKNOWN:
        return AffectedStatus.MISSING;
      default:
        return AffectedStatus.UNRECOGNIZED;
    }
  }
}
