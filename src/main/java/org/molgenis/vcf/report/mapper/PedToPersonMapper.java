package org.molgenis.vcf.report.mapper;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.molgenis.vcf.report.utils.PedReader;
import org.molgenis.vcf.report.utils.PedToPersonsParser;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

@Component
public class PedToPersonMapper {
  public Map<String, Person> mapPedFileToPersons(Path pedigreePath, int maxNrSamples) {
    Map<String, Person> persons;
    try (PedReader reader = new PedReader(new FileReader(pedigreePath.toFile()))) {
      persons = PedToPersonsParser.parse(reader, maxNrSamples);
    } catch (IOException e) {
      // this should never happen since the file was validated in the AppCommandLineOptions
      throw new IllegalStateException(e);
    }
    return persons;
  }
}
