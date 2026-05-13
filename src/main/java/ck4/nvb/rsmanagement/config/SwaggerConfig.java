package ck4.nvb.rsmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("RS MANAGEMENT CRUD API Documentation")
                .description(
                    "This is a simple CRUD API description for RESOURCE MANAGEMENT application.")
                .version("1.0.0"));
  }

  @Bean
  public GroupedOpenApi publicAPI() {
    return GroupedOpenApi.builder().group("rs-api").packagesToScan("ck4.nvb.rsmanagement").build();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
