package ck4.nvb.rsmanagement.base.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerial extends JsonSerializer<LocalDateTime> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Override
  public void serialize(
      LocalDateTime localDateTime,
      JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(localDateTime.format(formatter));
  }
}
