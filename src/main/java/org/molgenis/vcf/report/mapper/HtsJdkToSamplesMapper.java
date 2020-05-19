package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;

import htsjdk.variant.vcf.VCFHeader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Sample;
import org.springframework.stereotype.Component;

/**
 * @see VCFHeader
 * @see Sample
 */
@Component
public class HtsJdkToSamplesMapper {

  public Items<Sample> map(VCFHeader vcfHeader, int maxNrSamples) {
    List<Sample> samples;
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

      sampleNameToOffsetMap.forEach((sampleName, offset) -> {
        if (offset < maxNrSamples) {
          Sample sample = new Sample(sampleName);
          samples.set(offset, sample);
        }
      });
    }
    return new Items<>(samples, total);
  }
}
