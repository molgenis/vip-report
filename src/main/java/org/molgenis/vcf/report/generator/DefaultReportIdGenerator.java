package org.molgenis.vcf.report.generator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class DefaultReportIdGenerator implements ReportIdGenerator {
  @Override
  public String generate(Path htsFile) {
    byte[] hash;
    try {
      byte[] data = Files.readAllBytes(htsFile);
      hash = MessageDigest.getInstance("MD5").digest(data);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new BigInteger(1, hash).toString(16);
  }
}
