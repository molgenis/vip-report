package org.molgenis.vcf.report.utils;

import org.phenopackets.schema.v1.core.Pedigree.Person;

class PersonParser {
  public Person parse(String line) {
    String[] tokens = line.split("\\s+");
    if (tokens.length < 6) {
      throw new InvalidPedException(
          String.format("Invalid PED line, expected 6 columns on line: %s", line));
    }
    if (tokens.length > 6) {
      throw new UnsupportedPedException(
          String.format("Unsupported PED line, expected 6 columns on line: %s", line));
    }

    return Person.newBuilder().setFamilyId(tokens[0]).setIndividualId(tokens[1]).setPaternalId(tokens[2]).setMaternalId(tokens[3]).setSexValue(Integer.parseInt(tokens[4])).setAffectedStatusValue(Integer.parseInt(tokens[5])).build();
  }
}
