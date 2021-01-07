package org.molgenis.vcf.report.mapper.info;

import static java.util.Arrays.asList;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.List;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class VepInfoMetadataMapper extends AbstractInfoMetadataMapper {
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";

  @Override
  public boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
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
    String description = vcfInfoHeaderLine.getDescription();
    String[] infoIds = description.substring(INFO_DESCRIPTION_PREFIX.length()).split("\\|", -1);
    return asList(infoIds);
  }

  @Override
  protected InfoMetadata mapNestedId(String id) {
    InfoMetadata infoMetadata;
    switch (id) {
      case "Consequence":
      case "Existing_variation":
      case "CLIN_SIG":
      case "FLAGS":
      case "HPO":
      case "InheritanceModesGene":
        infoMetadata =
            InfoMetadata.builder()
                .id(id)
                .number(Number.builder().type(Type.OTHER).separator('&').build())
                .type(CompoundMetadata.Type.STRING)
                .description(id)
                .build();
        break;
      case "PHENO":
      case "PUBMED":
      case "SOMATIC":
        infoMetadata =
            InfoMetadata.builder()
                .id(id)
                .number(Number.builder().type(Type.OTHER).separator('&').build())
                .type(CompoundMetadata.Type.INTEGER)
                .description(id)
                .build();
        break;
      case "STRAND":
      case "HGNC_ID":
        infoMetadata =
            InfoMetadata.builder()
                .id(id)
                .number(Number.builder().type(Type.NUMBER).count(1).build())
                .type(CompoundMetadata.Type.INTEGER)
                .description(id)
                .build();
        break;
      case "gnomAD_AF":
      case "gnomAD_AFR_AF":
      case "gnomAD_AMR_AF":
      case "gnomAD_ASJ_AF":
      case "gnomAD_EAS_AF":
      case "gnomAD_FIN_AF":
      case "gnomAD_NFE_AF":
      case "gnomAD_OTH_AF":
      case "gnomAD_SAS_AF":
        infoMetadata =
            InfoMetadata.builder()
                .id(id)
                .number(Number.builder().type(Type.NUMBER).count(1).build())
                .type(CompoundMetadata.Type.FLOAT)
                .description(id)
                .build();
        break;
      default:
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
