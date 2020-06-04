package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

public class ObjectMapperConfigurer {

  private ObjectMapperConfigurer(){}

  public static void configure(ObjectMapper objectMapper) {
    objectMapper.addMixIn(Phenopacket.class, PhenopacketMixin.class);
    objectMapper.addMixIn(Person.class, PersonMixin.class);
    objectMapper.addMixIn(PhenotypicFeature.class, PhenotypicFeatureMixin.class);
    objectMapper.addMixIn(OntologyClass.class, OntologyClassMixin.class);
    objectMapper.addMixIn(Individual.class, IndividualMixin.class);

    objectMapper.setAnnotationIntrospector(new PhenopacketInoreSuperIntrospector());
  }
}
