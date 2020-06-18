package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.List;
import org.molgenis.vcf.report.mapper.info.InfoMetadataMapper;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToInfoMetadataMapper {

  private final List<InfoMetadataMapper> infoMetadataMappers;

  HtsJdkToInfoMetadataMapper(List<InfoMetadataMapper> infoMetadataMappers) {
    this.infoMetadataMappers = requireNonNull(infoMetadataMappers);
  }

  public InfoMetadata map(VCFInfoHeaderLine vcfInfoHeaderLine) {
    InfoMetadata infoMetadata;
    for (InfoMetadataMapper infoMetadataMapper : infoMetadataMappers) {
      if (infoMetadataMapper.canMap(vcfInfoHeaderLine)) {
        infoMetadata = infoMetadataMapper.map(vcfInfoHeaderLine);
        return infoMetadata;
      }
    }
    throw new UnsupportedOperationException(
        String.format("No parser found that can handle INFO ID=%s", vcfInfoHeaderLine.getID()));
  }
}
