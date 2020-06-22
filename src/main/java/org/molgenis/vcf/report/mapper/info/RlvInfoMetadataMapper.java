package org.molgenis.vcf.report.mapper.info;

import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.List;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.Type;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RlvInfoMetadataMapper extends AbstractInfoMetadataMapper {
  private static final String INFO_ID = "RLV";

  @Override
  public boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    return vcfInfoHeaderLine.getID().equals(INFO_ID);
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
    String[] tokens = vcfInfoHeaderLine.getDescription().split("\\|", -1);
    return Arrays.stream(tokens).map(String::trim).collect(toList());
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
    InfoMetadataUtils.validateNumberUnbounded(vcfInfoHeaderLine);
    InfoMetadataUtils.validateTypeString(vcfInfoHeaderLine);
  }
}
