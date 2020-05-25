package org.molgenis.vcf.report.utils;

import org.molgenis.vcf.report.utils.PedIndividual.AffectionStatus;
import org.molgenis.vcf.report.utils.PedIndividual.Sex;

class PedIndividualParser {
  public PedIndividual parse(String line) {
    String[] tokens = line.split("\\s+");
    if (tokens.length < 6) {
      throw new InvalidPedException(
          String.format("Invalid PED line, expected 6 columns on line: %s", line));
    }
    if (tokens.length > 6) {
      throw new UnsupportedPedException(
          String.format("Unsupported PED line, expected 6 columns on line: %s", line));
    }

    Sex sex = parseSex(tokens[4]);
    AffectionStatus affectionStatus = parseAffectionStatus(tokens[5]);
    return new PedIndividual(tokens[0], tokens[1], tokens[2], tokens[3], sex, affectionStatus);
  }

  private Sex parseSex(String token) {
    Sex sex;
    switch (token) {
      case "1":
        sex = Sex.MALE;
        break;
      case "2":
        sex = Sex.FEMALE;
        break;
      default:
        sex = Sex.UNKNOWN;
        break;
    }
    return sex;
  }

  private AffectionStatus parseAffectionStatus(String token) {
    AffectionStatus affectionStatus;
    switch (token) {
      case "-9":
      case "0":
        affectionStatus = AffectionStatus.UNKNOWN;
        break;
      case "1":
        affectionStatus = AffectionStatus.UNAFFECTED;
        break;
      case "2":
        affectionStatus = AffectionStatus.AFFECTED;
        break;
      default:
        throw new UnsupportedPedException(
            String.format(
                "Phenotype value '%s' that is not an affection status (-9, 0, 1 or 2) is unsupported",
                token));
    }
    return affectionStatus;
  }
}
