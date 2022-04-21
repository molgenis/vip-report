package org.molgenis.vcf.report.model;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BytesSerializerTest {
  @Mock private JsonGenerator jsonGenerator;
  @Mock private SerializerProvider serializerProvider;

  private BytesSerializer bytesSerializer;

  @BeforeEach
  void setUpBeforeEach() {
    bytesSerializer = new BytesSerializer();
  }

  @Test
  void serialize() throws IOException {
    bytesSerializer.serialize(new Bytes(new byte[] {0, 1, 2}), jsonGenerator, serializerProvider);
    verify(jsonGenerator).writeRawValue("new Uint8Array([0,1,2])");
  }
}
