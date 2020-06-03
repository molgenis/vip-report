package org.molgenis.vcf.report.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.stereotype.Component;

@Component
public class PhenopacketMapper {
  private static final String SAMPLE_PHENOTYPE_SEPARATOR = "/";
  private static final String PHENOTYPE_SEPARATOR = ";";

  public Items<Phenopacket> mapPhenotypes(String phenotypes, List<Person> persons) {
    List<Phenopacket> phenopackets = new ArrayList<>();
    List<SamplePhenotype> phenotypeList = parse(phenotypes);
    for (SamplePhenotype samplePhenotype : phenotypeList) {
      PhenotypeMode mode = samplePhenotype.getMode();
      switch (mode) {
        case STRING:
          createPhenopacketsForSamples(persons, phenopackets, samplePhenotype);
          break;
        case PER_SAMPLE_STRING:
          mapPhenotypes(phenopackets, samplePhenotype.getSubjectId(), samplePhenotype.getPhenotypes());
          break;
        default:
          throw new UnexpectedEnumException(mode);
      }
    }
    return new Items(phenopackets, phenopackets.size());
  }

  private void createPhenopacketsForSamples(
      List<Person> persons, List<Phenopacket> phenopackets, SamplePhenotype samplePhenotype) {
    for (Person person : persons) {
      if (person.getAffectedStatus() != AffectedStatus.UNAFFECTED) {
        mapPhenotypes(phenopackets, person.getIndividualId(), samplePhenotype.getPhenotypes());
      }
    }
  }

  private  void mapPhenotypes(
      List<Phenopacket> phenopackets, String sampleId, String[] phenotypeString) {
    Builder builder = Phenopacket.newBuilder();

    Individual individual = Individual.newBuilder().setId(sampleId).build();
    builder.setSubject(individual);

    for (String phenotype : phenotypeString) {
      checkPhenotype(phenotype);
      OntologyClass ontologyClass =
          OntologyClass.newBuilder().setId(phenotype).setLabel(phenotype).build();
      PhenotypicFeature phenotypicFeature =
          PhenotypicFeature.newBuilder().addModifiers(ontologyClass).build();
      builder.addPhenotypicFeatures(phenotypicFeature);
    }
    phenopackets.add(builder.build());
  }

  private void checkPhenotype(String phenotype) {
    Pattern p = Pattern.compile(".+:.+");
    Matcher m = p.matcher(phenotype);
    if(!m.matches()){
      throw new IllegalPhenotypeArgumentException(phenotype);
    }
  }

  private List<SamplePhenotype> parse(String phenotypesString) {
    if (phenotypesString.contains(SAMPLE_PHENOTYPE_SEPARATOR)) {
      return parseSamplePhenotypes(phenotypesString);
    } else {
      String[] phenotypes = phenotypesString.split(PHENOTYPE_SEPARATOR);
      return Collections.singletonList(new SamplePhenotype(PhenotypeMode.STRING, null, phenotypes));
    }
  }

  private List<SamplePhenotype> parseSamplePhenotypes(String phenotypesString) {
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
        throw new IllegalArgumentException("Mixing general phenotypes for all samples and phenotypes per sample is not allowed.");
      }
    }
    return result;
  }
}
