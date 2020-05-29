package org.molgenis.vcf.report.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.vcf.report.model.Items;
import org.phenopackets.schema.v1.core.Pedigree.Person;

public class PersonListMerger {
  public static Items<Person> merge(int maxNrSamples, List<Person> vcfPersons,
      Map<String, Person> pedigreePersons) {
    vcfPersons
        .forEach(
            person -> {
              if (!pedigreePersons.containsKey(person.getIndividualId()) && pedigreePersons.size() < maxNrSamples) {
                pedigreePersons.put(person.getIndividualId(), person);
              }
            });
    return new Items(pedigreePersons.values().stream().collect(Collectors.toList()), pedigreePersons.size());
  }
}
