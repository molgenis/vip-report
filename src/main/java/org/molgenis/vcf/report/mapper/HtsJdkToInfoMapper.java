package org.molgenis.vcf.report.mapper;

import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToInfoMapper extends HtsJdkToCompoundMapper<Info> {

  @Override
  public Info map(
      List<CompoundMetadata<Info>> compoundMetadataList, Map<String, Object> attributes) {
    Info info = new Info();
    attributes.forEach(
        (key, value) -> {
          CompoundMetadata<Info> compoundMetadata =
              compoundMetadataList.stream()
                  .filter(anInfoMetadata -> anInfoMetadata.getId().equals(key))
                  .findFirst()
                  .orElse(null);
          Object mappedValue = mapAttribute(compoundMetadata, value);
          info.put(key, mappedValue);
        });
    return info;
  }
}
