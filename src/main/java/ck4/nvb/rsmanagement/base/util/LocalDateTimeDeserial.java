package ck4.nvb.rsmanagement.base.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserial extends JsonDeserializer<LocalDateTime> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Override
  public LocalDateTime deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JacksonException {
    return LocalDateTime.parse(jsonParser.getText(), formatter);
  }
}
