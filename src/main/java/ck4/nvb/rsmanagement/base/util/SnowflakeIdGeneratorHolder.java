package ck4.nvb.rsmanagement.base.util;

public class SnowflakeIdGeneratorHolder {

  private static final SnowflakeIdGenerator INSTANCE = new SnowflakeIdGenerator(1, 1);

  public static SnowflakeIdGenerator getInstance() {
    return INSTANCE;
  }
}
