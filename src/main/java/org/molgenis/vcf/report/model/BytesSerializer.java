package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class BytesSerializer extends StdSerializer<Bytes> {

  public BytesSerializer() {
    super(Bytes.class);
  }

  @Override
  public void serialize(
      Bytes bytes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    StringBuilder stringBuilder = new StringBuilder("new Uint8Array([");
    byte[] byteArray = bytes.getBytes();
    for (int i = 0; i < byteArray.length; ++i) {
      if (i > 0) {
        stringBuilder.append(',');
      }
      stringBuilder.append(Byte.toUnsignedInt(byteArray[i]));
    }
    stringBuilder.append("])");
    jsonGenerator.writeRawValue(stringBuilder.toString());
  }
}
