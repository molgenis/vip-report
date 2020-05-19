package org.molgenis.vcf.report.mapper;

import htsjdk.variant.variantcontext.GenotypeType;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.Genotype.Type;
import org.springframework.stereotype.Component;

/**
 * @see GenotypeType
 * @see Type
 */
@Component
public class HtsJdkToGenotypeTypeMapper {

  public Type map(GenotypeType genotypeType) {
    Type type;
    switch (genotypeType) {
      case HET:
        type = Type.HETEROZYGOUS;
        break;
      case HOM_REF:
        type = Type.HOMOZYGOUS_REF;
        break;
      case HOM_VAR:
        type = Type.HOMOZYGOUS_ALT;
        break;
      case MIXED:
        type = Type.PARTIAL_CALL;
        break;
      case NO_CALL:
        type = Type.NO_CALL;
        break;
      case UNAVAILABLE:
      default:
        throw new UnexpectedEnumException(genotypeType);
    }
    return type;
  }
}
