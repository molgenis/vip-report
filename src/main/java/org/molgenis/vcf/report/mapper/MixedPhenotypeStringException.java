package org.molgenis.vcf.report.mapper;

public class MixedPhenotypeStringException extends RuntimeException {

  private static final String MESSAGE = "Mixing general phenotypes for all samples and phenotypes per sample is not allowed.";

  public MixedPhenotypeStringException() {
    super(MESSAGE);
  }
}
