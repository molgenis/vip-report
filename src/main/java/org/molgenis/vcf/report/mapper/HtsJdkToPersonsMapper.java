package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;

import htsjdk.variant.vcf.VCFHeader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.model.Items;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

/**
 * @see VCFHeader
 * @see Person
 */
@Component
public class HtsJdkToPersonsMapper {

  static final String MISSING = "MISSING_";
  static final String MISSING_PERSON_ID = "0";

  public Items<Person> map(VCFHeader vcfHeader, int maxNrSamples) {
    List<Person> samples;
    int total;

    if (!vcfHeader.hasGenotypingData()) {
      samples = emptyList();
      total = 0;
    } else {
      Map<String, Integer> sampleNameToOffsetMap = vcfHeader.getSampleNameToOffset();

      total = sampleNameToOffsetMap.size();
      int nrSamples = Math.min(total, maxNrSamples);
      samples = new ArrayList<>(nrSamples);
      for (int i = 0; i < nrSamples; ++i) {
        samples.add(null);
      }

      sampleNameToOffsetMap.forEach(
          (sampleName, offset) -> {
            if (offset < maxNrSamples) {
              //Paternal and maternal ID can be left empty, but this way we keep it consistent with the Persons loaded via the ped files.
              Person sample = Person.newBuilder().setIndividualId(sampleName).setPaternalId(MISSING_PERSON_ID).setMaternalId(MISSING_PERSON_ID).setFamilyId(
                  MISSING+offset).build();
              samples.set(offset, sample);
            }
          });
    }
    return new Items<>(samples, total);
  }
}
