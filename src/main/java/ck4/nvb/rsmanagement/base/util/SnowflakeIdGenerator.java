package ck4.nvb.rsmanagement.base.util;

public class SnowflakeIdGenerator {
  private final long twepoch = 1609459200000L; // 01/01/2021
  private final long datacenterIdBits = 5L;
  private final long workerIdBits = 5L;
  private final long sequenceBits = 12L;

  private final long maxDatacenterId = ~(-1L << datacenterIdBits);
  private final long maxWorkerId = ~(-1L << workerIdBits);

  private final long workerIdShift = sequenceBits;
  private final long datacenterIdShift = sequenceBits + workerIdBits;
  private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
  private final long sequenceMask = ~(-1L << sequenceBits);

  private long datacenterId;
  private long workerId;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  public SnowflakeIdGenerator(long datacenterId, long workerId) {
    if (datacenterId > maxDatacenterId || datacenterId < 0) {
      throw new IllegalArgumentException("Invalid datacenterId");
    }
    if (workerId > maxWorkerId || workerId < 0) {
      throw new IllegalArgumentException("Invalid workerId");
    }
    this.datacenterId = datacenterId;
    this.workerId = workerId;
  }

  public synchronized long nextId() {
    long timestamp = timeGen();
    if (timestamp < lastTimestamp) {
      throw new RuntimeException("Clock moved backwards.");
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0L;
    }

    lastTimestamp = timestamp;

    return ((timestamp - twepoch) << timestampLeftShift)
        | (datacenterId << datacenterIdShift)
        | (workerId << workerIdShift)
        | sequence;
  }

  private long tilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  private long timeGen() {
    return System.currentTimeMillis();
  }
}
