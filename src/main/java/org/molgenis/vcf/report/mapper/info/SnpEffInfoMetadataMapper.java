package org.molgenis.vcf.report.mapper.info;

import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SnpEffInfoMetadataMapper extends AbstractInfoMetadataMapper {
  private static final String INFO_ID = "ANN";
  private static final Pattern INFO_DESCRIPTION_PATTERN =
      Pattern.compile("Functional annotations: '(.*?)'");

  @Override
  public boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    return vcfInfoHeaderLine.getID().equals(INFO_ID)
        && INFO_DESCRIPTION_PATTERN.matcher(vcfInfoHeaderLine.getDescription()).matches();
  }

  @SuppressWarnings("unchecked")
  @Override
  public InfoMetadata map(VCFInfoHeaderLine vcfInfoHeaderLine) {
    validate(vcfInfoHeaderLine);

    List<InfoMetadata> nestedInfoMetaData = mapNested(vcfInfoHeaderLine);

    return InfoMetadata.builder()
        .id(vcfInfoHeaderLine.getID())
        .number(Number.builder().type(Type.OTHER).separator(',').build())
        .type(CompoundMetadata.Type.NESTED)
        .description(vcfInfoHeaderLine.getDescription())
        .source(vcfInfoHeaderLine.getSource())
        .version(vcfInfoHeaderLine.getVersion())
        .nestedMetadata((List<CompoundMetadata<Info>>) (List<?>) nestedInfoMetaData)
        .build();
  }

  @Override
  protected List<String> getNestedInfoIds(VCFInfoHeaderLine vcfInfoHeaderLine) {
    Matcher matcher = INFO_DESCRIPTION_PATTERN.matcher(vcfInfoHeaderLine.getDescription());
    List<String> infoIds;
    if (matcher.matches()) {
      String[] tokens = matcher.group(1).split("\\|", -1);
      infoIds = Arrays.stream(tokens).map(String::trim).collect(toList());
    } else {
      infoIds = Collections.emptyList();
    }
    return infoIds;
  }

  @Override
  protected InfoMetadata mapNestedId(String id) {
    InfoMetadata infoMetadata;
    if ("Annotation".equals(id)) {
      infoMetadata =
          InfoMetadata.builder()
              .id(id)
              .number(Number.builder().type(Type.OTHER).separator('&').build())
              .type(CompoundMetadata.Type.STRING)
              .description(id)
              .build();
    } else {
      infoMetadata = super.createNestedInfoMetadata(id);
    }
    return infoMetadata;
  }

  private void validate(VCFInfoHeaderLine vcfInfoHeaderLine) {
    if (!canMap(vcfInfoHeaderLine)) {
      throw new IllegalArgumentException(
          String.format("Can't map ##INFO=<ID=%s,...>", vcfInfoHeaderLine.getID()));
    }
    InfoMetadataUtils.validateNumberUnbounded(vcfInfoHeaderLine);
    InfoMetadataUtils.validateTypeString(vcfInfoHeaderLine);
  }
}
