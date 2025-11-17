package org.molgenis.vcf.report.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Base85EncoderTest {

  @Mock private BinaryEncoder binaryEncoder;
  private Base85Encoder base85Encoder;

  @BeforeEach
  void setUpBeforeEach() {
    base85Encoder = new Base85Encoder(binaryEncoder);
  }

  @Test
  void encode() {
    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    when(binaryEncoder.encode(inputVcfPath))
        .thenReturn("lorum ipsum".getBytes(StandardCharsets.UTF_8));
    assertEquals("Y;SUPZ6IlIb9HS", base85Encoder.encode(inputVcfPath));
  }
}
