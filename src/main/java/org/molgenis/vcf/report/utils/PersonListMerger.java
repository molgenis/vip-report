package org.molgenis.vcf.report.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class PersonListMerger {
  public Items<Sample> merge(
      List<Sample> vcfSamples, Map<String, Sample> pedigreeSamples, int maxNrSamples) {
    vcfSamples.forEach(
        sample -> {
          if (pedigreeSamples.containsKey(sample.getPerson().getIndividualId())
              && pedigreeSamples.size() < maxNrSamples) {
            Sample merged =
                new Sample(
                    pedigreeSamples.get(sample.getPerson().getIndividualId()).getPerson(),
                    sample.getIndex());
            pedigreeSamples.put(sample.getPerson().getIndividualId(), merged);
          }else{
            pedigreeSamples.put(sample.getPerson().getIndividualId(), sample);
          }
        });
    return new Items<>(
        pedigreeSamples.values().stream().collect(Collectors.toList()), pedigreeSamples.size());
  }
}
