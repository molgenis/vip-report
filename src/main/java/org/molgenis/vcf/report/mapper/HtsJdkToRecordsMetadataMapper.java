package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFHeader;
import java.util.List;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToRecordsMetadataMapper {

  private final HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper;

  HtsJdkToRecordsMetadataMapper(HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper) {
    this.htsJdkToInfoMetadataMapper = requireNonNull(htsJdkToInfoMetadataMapper);
  }

  public RecordsMetadata map(VCFHeader vcfHeader) {
    List<InfoMetadata> infoMetadataMap =
        vcfHeader.getInfoHeaderLines().stream()
            .map(htsJdkToInfoMetadataMapper::map)
            .collect(toList());
    return new RecordsMetadata(infoMetadataMap);
  }
}
