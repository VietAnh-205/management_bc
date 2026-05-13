package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.system;

import ck4.nvb.rsmanagement.base.util.SnowflakeIdGeneratorHolder;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.IdGeneratorPort;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGeneratorAdapter implements IdGeneratorPort {

  @Override
  public long nextId() {
    return SnowflakeIdGeneratorHolder.getInstance().nextId();
  }
}