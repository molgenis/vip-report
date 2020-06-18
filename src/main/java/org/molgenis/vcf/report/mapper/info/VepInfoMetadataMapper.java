package org.molgenis.vcf.report.mapper.info;

import static java.util.Arrays.asList;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Collections;
import java.util.List;
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

  @Override
  public InfoMetadata map(VCFInfoHeaderLine vcfInfoHeaderLine) {
    validate(vcfInfoHeaderLine);

    List<InfoMetadata> nestedInfoMetaData = mapNested(vcfInfoHeaderLine);

    return new InfoMetadata(
        vcfInfoHeaderLine.getID(),
        new Number(Type.OTHER, null, ','),
        InfoMetadata.Type.NESTED,
        vcfInfoHeaderLine.getDescription(),
        vcfInfoHeaderLine.getSource(),
        vcfInfoHeaderLine.getVersion(),
        nestedInfoMetaData);
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
      case "EXON":
      case "INTRON":
        infoMetadata =
            new InfoMetadata(
                id,
                new Number(Type.NUMBER, 2, '/'),
                InfoMetadata.Type.INTEGER,
                id,
                null,
                null,
                Collections.emptyList());
        break;
      case "Existing_variation":
        infoMetadata =
            new InfoMetadata(
                id,
                new Number(Type.OTHER, null, '&'),
                InfoMetadata.Type.STRING,
                id,
                null,
                null,
                Collections.emptyList());
        break;
      case "PHENO":
        infoMetadata =
            new InfoMetadata(
                id,
                new Number(Type.OTHER, null, '&'),
                InfoMetadata.Type.INTEGER,
                id,
                null,
                null,
                Collections.emptyList());
        break;
      case "STRAND":
      case "HGNC_ID":
        infoMetadata =
            new InfoMetadata(
                id,
                new Number(Type.NUMBER, 1, ','),
                InfoMetadata.Type.INTEGER,
                id,
                null,
                null,
                Collections.emptyList());
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
            new InfoMetadata(
                id,
                new Number(Type.NUMBER, 1, ','),
                InfoMetadata.Type.FLOAT,
                id,
                null,
                null,
                Collections.emptyList());
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
    validateCountType(vcfInfoHeaderLine);
    validateType(vcfInfoHeaderLine);
  }

  private void validateCountType(VCFInfoHeaderLine vcfInfoHeaderLine) {
    VCFHeaderLineCount countType = vcfInfoHeaderLine.getCountType();
    if (countType != VCFHeaderLineCount.UNBOUNDED) {
      String number =
          countType == VCFHeaderLineCount.INTEGER
              ? String.valueOf(vcfInfoHeaderLine.getCount())
              : countType.toString();

      throw new IllegalArgumentException(
          String.format(
              "Expected ##INFO=<ID=%s,Number=%s,...> to be of Number '%s'",
              vcfInfoHeaderLine.getID(), number, '.'));
    }
  }

  private void validateType(VCFInfoHeaderLine vcfInfoHeaderLine) {
    VCFHeaderLineType type = vcfInfoHeaderLine.getType();
    if (type != VCFHeaderLineType.String) {
      throw new IllegalArgumentException(
          String.format(
              "Expected ##INFO=<ID=%s,...,Type=%s,...> to be of type %s",
              vcfInfoHeaderLine.getID(), type, VCFHeaderLineType.String));
    }
  }
}
