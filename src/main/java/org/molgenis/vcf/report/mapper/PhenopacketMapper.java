package org.molgenis.vcf.report.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.PhenotypeMode;
import org.molgenis.vcf.report.model.SamplePhenotype;
import org.molgenis.vcf.report.utils.InvalidSamplePhenotypesException;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.Phenopacket.Builder;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

public class PhenopacketMapper {

  private PhenopacketMapper() { }

  private static final String SAMPLE_PHENOTYPE_SEPARATOR = "/";
  private static final String PHENOTYPE_SEPARATOR = ";";

  public static Items<Phenopacket> createPhenopackets(String phenotypes, List<Person> persons) {
    List<Phenopacket> phenopackets = new ArrayList<>();
    List<SamplePhenotype> phenotypeList = parse(phenotypes);
    for (SamplePhenotype samplePhenotype : phenotypeList) {
      PhenotypeMode mode = samplePhenotype.getMode();
      switch (mode) {
        case STRING:
          createPhenopacketsForSamples(persons, phenopackets, samplePhenotype);
          break;
        case PER_SAMPLE_STRING:
          for (String phenotype : samplePhenotype.getPhenotypes()) {
            createPhenopackets(phenopackets, samplePhenotype.getSubjectId(), phenotype.split(";"));
          }
          break;
        default:
          throw new UnexpectedEnumException(mode);
      }
    }
    return new Items(phenopackets, phenopackets.size());
  }

  private static void createPhenopacketsForSamples(
      List<Person> persons, List<Phenopacket> phenopackets, SamplePhenotype samplePhenotype) {
    for (Person person : persons) {
      if (person.getAffectedStatus() != AffectedStatus.UNAFFECTED) {
        createPhenopackets(phenopackets, person.getIndividualId(), samplePhenotype.getPhenotypes());
      }
    }
  }

  private static void createPhenopackets(
      List<Phenopacket> phenopackets, String sampleId, String[] phenotypeString) {
    Builder builder = Phenopacket.newBuilder();

    Individual individual = Individual.newBuilder().setId(sampleId).build();
    builder.setSubject(individual);

    for (String phenotype : phenotypeString) {
      OntologyClass ontologyClass =
          OntologyClass.newBuilder().setId(phenotype).setLabel(phenotype).build();
      PhenotypicFeature phenotypicFeature =
          PhenotypicFeature.newBuilder().addModifiers(ontologyClass).build();
      builder.addPhenotypicFeatures(phenotypicFeature);
    }
    phenopackets.add(builder.build());
  }

  private static List<SamplePhenotype> parse(String phenotypesString) {
    if (phenotypesString.contains(SAMPLE_PHENOTYPE_SEPARATOR)) {
      return parseSamplePhenotypes(phenotypesString);
    } else {
      String[] phenotypes = phenotypesString.split(PHENOTYPE_SEPARATOR);
      return Collections.singletonList(new SamplePhenotype(PhenotypeMode.STRING, null, phenotypes));
    }
  }

  private static List<SamplePhenotype> parseSamplePhenotypes(String phenotypesString) {
    List<SamplePhenotype> result = new ArrayList<>();
    for (String samplePhenotypes : phenotypesString.split(",")) {
      if (samplePhenotypes.contains("/")) {
        String[] split = samplePhenotypes.split("/");
        if (split.length == 2) {
          String sampleId = split[0];
          String[] phenotypes = split[1].split(";");
          result.add(new SamplePhenotype(PhenotypeMode.PER_SAMPLE_STRING, sampleId, phenotypes));
        } else {
          throw new InvalidSamplePhenotypesException(samplePhenotypes);
        }
      } else {
        throw new MixedPhenotypeStringException();
      }
    }
    return result;
  }
}
