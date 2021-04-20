package org.molgenis.vcf.report.generator;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import org.molgenis.vcf.report.utils.Base85;
import org.springframework.stereotype.Component;

@Component
public class Base85Encoder {

  private final BinaryEncoder binaryEncoder;

  Base85Encoder(BinaryEncoder binaryEncoder) {
    this.binaryEncoder = requireNonNull(binaryEncoder);
  }

  public String encode(Path path) {
    byte[] bytes = binaryEncoder.encode(path);
    return Base85.getRfc1924Encoder().encodeToString(bytes);
  }
}
