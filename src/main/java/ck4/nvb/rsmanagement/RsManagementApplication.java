package ck4.nvb.rsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RsManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(RsManagementApplication.class, args);
  }
}
