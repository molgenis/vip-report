package org.molgenis.vcf.report.mapper.info;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;

public abstract class AbstractInfoMetadataMapper implements InfoMetadataMapper {
  protected abstract List<String> getNestedInfoIds(VCFInfoHeaderLine vcfInfoHeaderLine);

  protected abstract InfoMetadata mapNestedId(String id);

  protected List<InfoMetadata> mapNested(VCFInfoHeaderLine vcfInfoHeaderLine) {
    List<String> ids = getNestedInfoIds(vcfInfoHeaderLine);

    List<InfoMetadata> infoMetadataList;
    if (!ids.isEmpty()) {
      infoMetadataList = new ArrayList<>(ids.size());
      for (String id : ids) {
        InfoMetadata infoMetadata = mapNestedId(id);
        infoMetadataList.add(infoMetadata);
      }
    } else {
      infoMetadataList = Collections.emptyList();
    }
    return infoMetadataList;
  }

  protected InfoMetadata createNestedInfoMetadata(String id) {
    return InfoMetadata.builder()
        .id(id)
        .number(Number.builder().type(Type.NUMBER).count(1).build())
        .type(CompoundMetadata.Type.STRING)
        .description(id)
        .build();
  }
}
