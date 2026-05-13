package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.system;

import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ClockPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort {

  private final ZoneId zoneId = ZoneId.systemDefault();

  @Override
  public LocalDate today() {
    return LocalDate.now(zoneId);
  }

  @Override
  public Instant nowInstant() {
    return Instant.now();
  }
}