package org.molgenis.vcf.report.mapper.info;

import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    return super.createNestedInfoMetadata(id);
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
