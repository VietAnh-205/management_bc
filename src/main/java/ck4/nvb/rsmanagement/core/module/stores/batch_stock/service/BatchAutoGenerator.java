package ck4.nvb.rsmanagement.core.module.stores.batch_stock.service;

import ck4.nvb.rsmanagement.core.module.stores.batch.domain.Batch;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItem;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStockRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.Supplier;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.SupplierRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("batchAutoGenerator")
@ConditionalOnProperty(name = "replenishment.enabled", havingValue = "true", matchIfMissing = false)
public class BatchAutoGenerator {

  private static final Logger log = LoggerFactory.getLogger(BatchAutoGenerator.class);

  private final BatchRepository batchRepository;
  private final BatchItemRepository batchItemRepository;
  private final BatchStockRepository batchStockRepository;
  private final ProductRepository productRepository;
  private final SupplierRepository supplierRepository;
  private final JdbcTemplate jdbcTemplate;

  @Value("${replenishment.cron}")
  private String replenishmentCron;

  @Value("${replenishment.lead-time-days}")
  private int defaultLeadTimeDays;

  @Value("${replenishment.min-order-qty}")
  private int minOrderQty;

  @Value("${replenishment.target-cover-days}")
  private int targetCoverDays;

  @Value("${replenishment.safety-stock-days}")
  private int safetyStockDays;

  @Value("${replenishment.min-daily-demand}")
  private double minDailyDemand;

  @Value("${replenishment.max-orders-per-run}")
  private int maxOrdersPerRun;

  @Value("${replenishment.max-order-limit}")
  private int maxOrderLimit;

  public BatchAutoGenerator(
      BatchRepository batchRepository,
      BatchItemRepository batchItemRepository,
      BatchStockRepository batchStockRepository,
      ProductRepository productRepository,
      SupplierRepository supplierRepository,
      JdbcTemplate jdbcTemplate) {
    this.batchRepository = batchRepository;
    this.batchItemRepository = batchItemRepository;
    this.batchStockRepository = batchStockRepository;
    this.productRepository = productRepository;
    this.supplierRepository = supplierRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostConstruct
  public void init() {
    log.info(
        "BatchAutoGenerator initialized. leadTimeDays={}, minOrderQty={}, targetCoverDays={}",
        defaultLeadTimeDays,
        minOrderQty,
        targetCoverDays);
  }

  @Scheduled(cron = "${replenishment.cron}")
  public void scheduledReplenishmentScan() {
    try {
      log.debug("Running scheduled replenishment scan at {}", LocalDateTime.now());
      performReplenishmentScan();
    } catch (Exception ex) {
      log.error("Replenishment scan failed", ex);
    }
  }

  public void performReplenishmentScan() {
    List<Product> products = productRepository.findAll();
    if (products == null || products.isEmpty()) {
      log.info("No products found; skipping replenishment scan.");
      return;
    }

    List<Long> stores =
        jdbcTemplate.queryForList(
            "SELECT DISTINCT store_id FROM batch_stock WHERE deleted = false", Long.class);
    if (stores == null || stores.isEmpty()) {
      log.info("No stores discovered in batch_stock; skipping.");
      return;
    }

    int created = 0;
    List<StoreProductPair> pairs = gatherLowInventoryPairsAggregate(products, stores);

    for (StoreProductPair pair : pairs) {
      if (created >= maxOrdersPerRun) break;
      try {
        boolean r = checkAndReplenish(pair.storeId, pair.productId);
        if (r) created++;
      } catch (Exception ex) {
        log.error(
            "Failed to handle replenishment for store {} product {}: {}",
            pair.storeId,
            pair.productId,
            ex.getMessage(),
            ex);
      }
    }

    log.info("Replenishment scan finished, createdOrders={}", created);
  }

  /**
   * Bulk gather store-product pairs ordered by urgency (lowest available first). This uses two
   * aggregate queries (ACTIVE availability and IN_TRANSIT incoming) and merges results in-memory.
   */
  private List<StoreProductPair> gatherLowInventoryPairsAggregate(
      List<Product> products, List<Long> stores) {
    Map<String, Long> availableMap = new HashMap<>();
    Map<String, Long> incomingMap = new HashMap<>();

    // Availability for ACTIVE batches
    String availSql =
        "SELECT bs.store_id as store_id, bi.product_id as product_id, COALESCE(SUM(bi.original_qty),0) as available "
            + "FROM batch_item bi "
            + "JOIN batch b ON b.id = bi.batch_id AND b.deleted = false "
            + "JOIN batch_stock bs ON bs.batch_id = b.id AND bs.deleted = false AND bs.status = 'ACTIVE' "
            + "WHERE bi.deleted = false "
            + "GROUP BY bs.store_id, bi.product_id";

    List<Map<String, Object>> availRows = jdbcTemplate.queryForList(availSql);
    for (Map<String, Object> r : availRows) {
      Long sid = ((Number) r.get("store_id")).longValue();
      Long pid = ((Number) r.get("product_id")).longValue();
      Number n = (Number) r.get("available");
      long val = n == null ? 0L : n.longValue();
      availableMap.put(key(sid, pid), val);
    }

    // Incoming for IN_TRANSIT batches
    String incomingSql =
        "SELECT bs.store_id as store_id, bi.product_id as product_id, COALESCE(SUM(bi.original_qty),0) as incoming "
            + "FROM batch_item bi "
            + "JOIN batch b ON b.id = bi.batch_id AND b.deleted = false "
            + "JOIN batch_stock bs ON bs.batch_id = b.id AND bs.deleted = false AND bs.status = 'IN_TRANSIT' "
            + "WHERE bi.deleted = false "
            + "GROUP BY bs.store_id, bi.product_id";

    List<Map<String, Object>> incRows = jdbcTemplate.queryForList(incomingSql);
    for (Map<String, Object> r : incRows) {
      Long sid = ((Number) r.get("store_id")).longValue();
      Long pid = ((Number) r.get("product_id")).longValue();
      Number n = (Number) r.get("incoming");
      long val = n == null ? 0L : n.longValue();
      incomingMap.put(key(sid, pid), val);
    }

    List<StoreProductPair> out = new ArrayList<>();
    for (Long storeId : stores) {
      for (Product p : products) {
        String k = key(storeId, p.getId());
        long avail = availableMap.getOrDefault(k, 0L);
        long incoming = incomingMap.getOrDefault(k, 0L);
        // we prioritize by netAvailable = avail + incoming so we focus on pairs where supply is low
        long netAvailable = avail + incoming;
        out.add(new StoreProductPair(storeId, p.getId(), netAvailable));
      }
    }
    out.sort(Comparator.comparingLong(a -> a.available));
    return out;
  }

  private String key(Long storeId, Long productId) {
    return storeId + ":" + productId;
  }

  private boolean checkAndReplenish(Long storeId, Long productId) {
    // re-calc to reduce chance of duplicate orders
    long available =
        Optional.ofNullable(
                batchItemRepository.getTotalAvailableQtyForProductInStore(productId, storeId))
            .orElse(0L);
    long incoming = getIncomingQtyForProductAndStore(productId, storeId);

    DemandStats stats = estimateDemand(productId, storeId, 30); // 30 days history
    double avgDaily = Math.max(stats.avgDaily, minDailyDemand);

    int lead = defaultLeadTimeDays;

    int reorderPoint = computeReorderPoint(avgDaily, stats.stddevDaily, lead, safetyStockDays);

    log.debug(
        "Store {} Product {}: available={}, incoming={}, avgDaily={}, stddev={}, reorderPoint={}",
        storeId,
        productId,
        available,
        incoming,
        avgDaily,
        stats.stddevDaily,
        reorderPoint);

    if (available + incoming > reorderPoint) {
      return false;
    }

    long desired = (long) Math.ceil((lead + targetCoverDays) * avgDaily);
    desired = Math.max(desired, minOrderQty);

    long toOrder = Math.max(0L, desired - (available + incoming));
    if (toOrder <= 0) return false;

    toOrder = roundToPackSize((int) toOrder, minOrderQty);

    // safety cap
    if (maxOrderLimit > 0 && toOrder > maxOrderLimit) {
      log.warn("Calculated toOrder={} exceeds maxOrderLimit={} — capping", toOrder, maxOrderLimit);
      toOrder = maxOrderLimit;
    }

    Long supplierId = chooseSupplierForProduct(productId);

    // Quick de-dup: re-check incoming after all calculations to avoid race
    long incomingNow = getIncomingQtyForProductAndStore(productId, storeId);
    if (available + incomingNow > reorderPoint) {
      log.info(
          "Skipping creation because incoming increased in the meantime for store={} product={}",
          storeId,
          productId);
      return false;
    }

    createIncomingBatch(productId, storeId, (int) toOrder, supplierId, lead);

    log.info(
        "Created replenishment for store={} product={} qty={} supplier={} ETA days={}",
        storeId,
        productId,
        toOrder,
        supplierId,
        lead);
    return true;
  }

  private int roundToPackSize(int qty, int pack) {
    if (pack <= 1) return qty;
    int mul = (qty + pack - 1) / pack;
    return Math.max(pack, mul * pack);
  }

  private DemandStats estimateDemand(Long productId, Long storeId, int windowDays) {
    // compute since using LocalDateTime
    LocalDateTime sinceLdt = LocalDateTime.now().minusDays(windowDays);
    Timestamp sinceTs = Timestamp.valueOf(sinceLdt); // chuyển sang Timestamp cho JDBC

    String sql =
        "SELECT DATE(so.created_at) as d, COALESCE(SUM(sl.qty_ordered),0) as qty "
            + "FROM sale_line sl "
            + "JOIN sale_order so ON so.id = sl.sale_order_id "
            + "WHERE sl.product_id = ? AND so.store_id = ? AND so.created_at >= ? "
            + "GROUP BY DATE(so.created_at) "
            + "ORDER BY DATE(so.created_at)";

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, productId, storeId, sinceTs);
    if (rows == null || rows.isEmpty()) {
      return new DemandStats(0.0, 0.0);
    }

    // Compose list of observed daily quantities (only days that have rows)
    List<Double> daily = new ArrayList<>();
    for (Map<String, Object> r : rows) {
      Number n = (Number) r.get("qty");
      daily.add(n == null ? 0.0 : n.doubleValue());
    }

    double sum = daily.stream().mapToDouble(Double::doubleValue).sum();
    // average across the full window (include zero days)
    double avg = sum / Math.max(1, windowDays);

    // compute variance including zero-days
    double meanObserved = sum / windowDays;
    double var = 0.0;
    for (double v : daily) var += (v - meanObserved) * (v - meanObserved);
    int zeros = windowDays - daily.size();
    var += zeros * (meanObserved * meanObserved);
    var = var / Math.max(1, windowDays);
    double stddev = Math.sqrt(var);

    return new DemandStats(avg, stddev);
  }

  private int computeReorderPoint(
      double avgDaily, double stddevDaily, int leadDays, int safetyDays) {
    double demandDuringLead = avgDaily * leadDays;
    double sigmaLead = stddevDaily * Math.sqrt(Math.max(1, leadDays));
    double z = 1.65; // target service level (~95%) - could be made configurable
    double safetyStock = z * sigmaLead + safetyDays * avgDaily * 0.1; // mild additional buffer
    int rop = (int) Math.ceil(demandDuringLead + safetyStock);
    return Math.max(1, rop);
  }

  private long getIncomingQtyForProductAndStore(Long productId, Long storeId) {
    String sql =
        "SELECT COALESCE(SUM(bi.original_qty),0) FROM batch_item bi "
            + "JOIN batch b ON b.id = bi.batch_id "
            + "JOIN batch_stock bs ON bs.batch_id = b.id "
            + "WHERE bi.product_id = ? AND bs.store_id = ? AND bs.status = 'IN_TRANSIT' "
            + "AND bi.deleted = false AND bs.deleted = false AND b.deleted = false";
    Number n = jdbcTemplate.queryForObject(sql, new Object[] {productId, storeId}, Number.class);
    return n == null ? 0L : n.longValue();
  }

  private Long chooseSupplierForProduct(Long productId) {
    // prefer the most recent historical supplier for product (if available)
    List<Long> supplierIds =
        batchItemRepository.findByProductId(productId).stream()
            .map(BatchItem::getSupplierId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (!supplierIds.isEmpty()) {
      // pick the most recent supplier in history (last element) to bias towards recent partners
      return supplierIds.get(supplierIds.size() - 1);
    }
    // fallback: random existing supplier
    List<Supplier> suppliers = supplierRepository.findAll();
    if (suppliers == null || suppliers.isEmpty()) return null;
    return suppliers.get(ThreadLocalRandom.current().nextInt(suppliers.size())).getId();
  }

  @Transactional
  public void createIncomingBatch(
      Long productId, Long storeId, int qty, Long supplierId, int leadDays) {
    LocalDateTime now = LocalDateTime.now();
    String shortUuid = UUID.randomUUID().toString().substring(0, 8);
    String batchCode = "B-" + shortUuidBase64();
    Batch b = new Batch();
    b.setBatchCode(batchCode);
    b.setCreatedTime(now);
    b.setUpdatedTime(now);
    Batch savedBatch = batchRepository.save(b);

    BatchItem bi = new BatchItem();
    bi.setBatchId(savedBatch.getId());
    bi.setProductId(productId);
    bi.setSupplierId(supplierId);
    bi.setOriginalQty(qty);
    Product p = productRepository.findById(productId).orElse(null);
    int unitPrice = p == null ? 0 : Optional.ofNullable(p.getUnitPrice()).orElse(0);
    bi.setImportPrice((int) Math.round(unitPrice * 0.7));
    bi.setManufactureDate(now.minusDays(2));
    bi.setExpiryDate(now.plusDays(180));
    bi.setCreatedTime(now);
    bi.setUpdatedTime(now);
    batchItemRepository.save(bi);

    BatchStock bs = new BatchStock();
    bs.setBatchId(savedBatch.getId());
    bs.setStoreId(storeId);
    bs.setStatus("IN_TRANSIT");
    bs.setVersion(0);
    bs.setCreatedTime(now);
    try {
      bs.getClass().getMethod("setEtaAt", LocalDateTime.class).invoke(bs, now.plusDays(leadDays));
    } catch (Exception ex) {
      bs.setUpdatedTime(now.plusDays(leadDays));
    }
    batchStockRepository.save(bs);
  }

  @Scheduled(fixedDelayString = "${replenishment.activation-interval-ms:60000}")
  @Transactional
  public void activateIncomingBatches() {
    LocalDateTime now = LocalDateTime.now();
    String sqlEta =
        "SELECT bs.id FROM batch_stock bs WHERE bs.status = 'IN_TRANSIT' AND bs.eta_at <= now() AND bs.deleted = false";
    List<Long> ids;
    try {
      ids = jdbcTemplate.queryForList(sqlEta, Long.class);
    } catch (Exception ex) {
      String sql =
          "SELECT bs.id FROM batch_stock bs WHERE bs.status = 'IN_TRANSIT' AND bs.updated_at <= now() AND bs.deleted = false";
      ids = jdbcTemplate.queryForList(sql, Long.class);
    }
    if (ids == null || ids.isEmpty()) return;
    for (Long id : ids) {
      try {
        BatchStock bs = batchStockRepository.findById(id).orElse(null);
        if (bs == null) continue;
        bs.setStatus("ACTIVE");
        bs.setUpdatedTime(now);
        batchStockRepository.save(bs);

        log.info(
            "Activated incoming batch_stock id={} batch_id={} for store={} (now available)",
            bs.getId(),
            bs.getBatchId(),
            bs.getStoreId());
      } catch (Exception ex) {
        log.error("Failed to activate batch_stock id={}", id, ex);
      }
    }
  }

  private String shortUuidBase64() {
    UUID u = UUID.randomUUID();
    ByteBuffer bb = ByteBuffer.allocate(16);
    bb.putLong(u.getMostSignificantBits());
    bb.putLong(u.getLeastSignificantBits());
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
  }

  private static class StoreProductPair {
    final Long storeId;
    final Long productId;
    final long available;

    StoreProductPair(Long storeId, Long productId, long available) {
      this.storeId = storeId;
      this.productId = productId;
      this.available = available;
    }
  }

  private static class DemandStats {
    final double avgDaily;
    final double stddevDaily;

    DemandStats(double avgDaily, double stddevDaily) {
      this.avgDaily = avgDaily;
      this.stddevDaily = stddevDaily;
    }
  }
}
