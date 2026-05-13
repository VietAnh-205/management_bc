package ck4.nvb.rsmanagement.core.module.forecasting.application.port.out;

import java.time.Instant;
import java.time.LocalDate;

public interface ClockPort {
  LocalDate today();
  Instant nowInstant();
}