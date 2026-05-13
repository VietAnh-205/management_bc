package ck4.nvb.rsmanagement.base.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.Getter;

public class JacksonParser {

  @Getter private static final JacksonParser instance = new JacksonParser();

  @Getter private final ObjectMapper objectMapper;

  private JacksonParser() {
    objectMapper = new ObjectMapper();

    JavaTimeModule timeModule = new JavaTimeModule();
    timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerial());
    timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserial());

    objectMapper.registerModule(timeModule);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Convert java object to JSON string
   *
   * @param obj - object to be converted
   * @return String - converted string
   */
  public String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Couldn't serialize object", e);
    }
  }

  /**
   * Convert JSON string to java object
   *
   * @param json
   * @param clazz
   * @return A Java object
   * @param <T>
   */
  public <T> T fromJson(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Couldn't deserialize object", e);
    }
  }
}
