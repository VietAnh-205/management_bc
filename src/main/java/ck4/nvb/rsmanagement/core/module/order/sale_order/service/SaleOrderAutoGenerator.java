package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.CustomerRepository;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethodRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocation;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocationRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLine;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLineRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrder;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrderRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleLineDto;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.Voucher;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItem;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStockRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
    name = "order.generator.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class SaleOrderAutoGenerator {

  private final SaleOrderServiceImpl saleOrderService;
  private final ISaleAllocationService saleAllocationService;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final PaymentMethodRepository paymentMethodRepository;
  private final VoucherRepository voucherRepository;
  private final BatchStockRepository batchStockRepository;
  private final BatchItemRepository batchItemRepository;
  private final SaleLineRepository saleLineRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final SaleAllocationRepository saleAllocationRepository;

  // tuning params (can be moved to config)
  @Value("${order.generator.daily-total}")
  private int dailyTotalOrders;

  @Value("${order.generator.realtime-hour-weights}")
  private String realtimeHourWeightsProp;

  @Value("${order.generator.max-realtime-orders}")
  private int maxRealtimeOrders;

  // enable/disable extra features
  @Value("${order.generator.enable-rfm}")
  private boolean enableRfm = true;

  @Value("${order.generator.enable-abandonment}")
  private boolean enableCartAbandonment = true;

  @Value("${order.generator.safety-stock-pct}")
  private double safetyStockPct = 0.05; // giữ lại 5% stock cho safety

  // softmax temperature: >1 more uniform, <1 more peaky
  private double samplingTemperature = 1.0;

  // seasonal multipliers per category-key (month indexed 0..11)
  private final Map<String, double[]> seasonalMultipliers = new HashMap<>();

  // hour boosts per category-key
  private final Map<String, double[]> hourBoosts = new HashMap<>();

  // day-of-week boosts per category-key
  private final Map<String, Double> dowBoosts = new HashMap<>();

  // simple holiday list (extendable) - when holiday -> bigger baskets
  private final Set<MonthDay> simpleHolidays = new HashSet<>();

  public SaleOrderAutoGenerator(
      SaleOrderServiceImpl saleOrderService,
      ISaleAllocationService saleAllocationService,
      CustomerRepository customerRepository,
      ProductRepository productRepository,
      PaymentMethodRepository paymentMethodRepository,
      VoucherRepository voucherRepository,
      BatchStockRepository batchStockRepository,
      BatchItemRepository batchItemRepository,
      SaleLineRepository saleLineRepository,
      SaleOrderRepository saleOrderRepository,
      SaleAllocationRepository saleAllocationRepository) {
    this.saleOrderService = saleOrderService;
    this.saleAllocationService = saleAllocationService;
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
    this.paymentMethodRepository = paymentMethodRepository;
    this.voucherRepository = voucherRepository;
    this.batchStockRepository = batchStockRepository;
    this.batchItemRepository = batchItemRepository;
    this.saleLineRepository = saleLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.saleAllocationRepository = saleAllocationRepository;

    seasonalMultipliers.put(
        "drink", new double[] {1.0, 1.0, 1.0, 1.0, 1.05, 1.1, 1.2, 1.25, 1.15, 1.05, 1.0, 1.0});
    seasonalMultipliers.put(
        "fruit", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.3, 1.25, 1.1, 1.0, 1.0});
    seasonalMultipliers.put(
        "snack", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.05, 1.05, 1.05, 1.05, 1.0, 1.0, 1.0});
    seasonalMultipliers.put(
        "coffee", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.05, 1.1});

    hourBoosts.put(
        "drink",
        createHourBoostArray(24, 1.0, new int[][] {{11, 13, 14}}, new double[] {1.4})); // lunch
    hourBoosts.put(
        "coffee", createHourBoostArray(24, 1.0, new int[][] {{6, 9}}, new double[] {1.5}));
    hourBoosts.put(
        "snack", createHourBoostArray(24, 1.0, new int[][] {{17, 20}}, new double[] {1.3}));

    dowBoosts.put("weekend_snack", 1.15);

    simpleHolidays.add(MonthDay.of(1, 1));
    simpleHolidays.add(MonthDay.of(12, 25));
  }

  private double[] createHourBoostArray(
      int length, double defaultVal, int[][] ranges, double[] boosts) {
    double[] arr = new double[length];
    Arrays.fill(arr, defaultVal);
    for (int i = 0; i < ranges.length; i++) {
      int from = ranges[i][0];
      int to = ranges[i][1];
      double b = boosts[i];
      for (int h = from; h <= to && h < length; h++) arr[h] = b;
    }
    return arr;
  }

  private LocalDateTime nowLocal() {
    return LocalDateTime.now(ZoneId.systemDefault());
  }

  // ---------------- realtime generator ----------------
  @Scheduled(fixedRateString = "${order.generator.interval}")
  public void scheduledGenerateRealtime() {
    try {
      String prop = realtimeHourWeightsProp == null ? "" : realtimeHourWeightsProp.trim();
      String[] parts = prop.isEmpty() ? new String[0] : prop.split("\\s*,\\s*");
      int[] weights = new int[24];
      boolean parsed = false;
      if (parts.length == 24) {
        parsed = true;
        for (int i = 0; i < 24; i++) {
          try {
            weights[i] = Math.max(0, Integer.parseInt(parts[i]));
          } catch (Exception e) {
            weights[i] = 0;
          }
        }
      }
      if (!parsed) {
        weights =
            new int[] {1, 1, 2, 3, 4, 3, 2, 3, 4, 3, 2, 1, 2, 3, 4, 3, 2, 1, 1, 3, 4, 3, 1, 1};
      }

      int currentHour = nowLocal().getHour();
      int weight = (currentHour >= 0 && currentHour < weights.length) ? weights[currentHour] : 1;
      int maxWeight = Arrays.stream(weights).max().orElse(1);
      int scaledMaxOrders = 0;
      if (maxWeight > 0) {
        double ratio = weight / (double) maxWeight;
        scaledMaxOrders = 1 + (int) Math.round(ratio * (Math.max(1, maxRealtimeOrders) - 1));
      }
      scaledMaxOrders = Math.max(0, scaledMaxOrders);

      if (scaledMaxOrders <= 0) return;

      int ordersToGenerate = ThreadLocalRandom.current().nextInt(1, scaledMaxOrders + 1);
      for (int i = 0; i < ordersToGenerate; i++) generateOneOrderRealtime();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Transactional
  public void generateOneOrderRealtime() {
    // load base lists
    List<Customer> customers = customerRepository.findAll();
    List<Product> products = productRepository.findAll();
    List<PaymentMethod> payments = paymentMethodRepository.findAll();
    List<Voucher> vouchers = voucherRepository.findAll();

    if (products.isEmpty() || payments.isEmpty()) {
      System.out.println("Skip: not enough base data (products/payments).");
      return;
    }

    // advanced classification: produce segments map
    Map<String, List<Customer>> segments = classifyCustomersAdvanced(customers);

    // derive active stores from batch stocks
    List<BatchStock> activeBatchStocks =
        batchStockRepository.findAll().stream()
            .filter(
                bs ->
                    bs != null
                        && bs.getStatus() != null
                        && "ACTIVE".equalsIgnoreCase(bs.getStatus()))
            .collect(Collectors.toList());
    if (activeBatchStocks.isEmpty()) {
      System.out.println("Skip: no active batch stocks (no store inventory).");
      return;
    }

    List<Long> stores =
        activeBatchStocks.stream()
            .map(BatchStock::getStoreId)
            .distinct()
            .collect(Collectors.toList());
    if (stores.isEmpty()) {
      System.out.println("Skip: no stores found from batch stocks.");
      return;
    }

    double[] storeWeights = new double[stores.size()];
    for (int i = 0; i < stores.size(); i++) storeWeights[i] = Math.max(1.0, 25.0 - i * 1.2);
    Long chosenStoreId = weightedChoiceLong(stores, storeWeights);
    if (chosenStoreId == null) {
      System.out.println("Skip: couldn't choose a store.");
      return;
    }

    // pick a customer segment probabilistically
    double r = ThreadLocalRandom.current().nextDouble();
    Customer chosenCustomer = null;
    String chosenSegment = "guest";
    if (r < 0.10 && !segments.getOrDefault("vip", Collections.emptyList()).isEmpty()) {
      chosenSegment = "vip";
      chosenCustomer = randomFromList(segments.get("vip"));
    } else if (r < 0.55 && !segments.getOrDefault("regular", Collections.emptyList()).isEmpty()) {
      chosenSegment = "regular";
      chosenCustomer = randomFromList(segments.get("regular"));
    } else if (r < 0.8 && !segments.getOrDefault("newcomer", Collections.emptyList()).isEmpty()) {
      chosenSegment = "newcomer";
      chosenCustomer = randomFromList(segments.get("newcomer"));
    } else {
      // guest
      chosenSegment = "guest";
      chosenCustomer = null;
    }

    // payment weight baselines per segment
    double[] paymentWeights;
    switch (chosenSegment) {
      case "vip":
        paymentWeights = new double[] {10, 30, 30, 20, 10};
        break;
      case "regular":
        paymentWeights = new double[] {40, 25, 20, 10, 5};
        break;
      case "newcomer":
        paymentWeights = new double[] {50, 20, 15, 10, 5};
        break;
      default:
        paymentWeights = new double[] {70, 15, 10, 3, 2};
        break;
    }
    PaymentMethod chosenPayment =
        weightedChoicePayment(paymentMethodRepository.findAll(), paymentWeights);

    // voucher decision: stricter for guest
    boolean maybeUseVoucher =
        ThreadLocalRandom.current().nextDouble()
            < (chosenCustomer != null
                    && segments
                        .getOrDefault("vip", Collections.emptyList())
                        .contains(chosenCustomer)
                ? 0.35
                : (chosenSegment.equals("guest") ? 0.03 : 0.12));
    Voucher chosenVoucher = null;
    if (maybeUseVoucher && !vouchers.isEmpty()) {
      LocalDateTime nowTime = nowLocal();
      Customer finalChosenCustomer = chosenCustomer;
      List<Voucher> applicable =
          vouchers.stream()
              .filter(
                  v -> {
                    if (v == null) return false;
                    try {
                      LocalDateTime from = v.getValidFrom();
                      LocalDateTime to = v.getValidTo();
                      if (from != null && nowTime.isBefore(from)) return false;
                      if (to != null && nowTime.isAfter(to)) return false;
                    } catch (Exception e) {
                    }
                    // check qty availability
                    if (v.getQtyTotal() != null
                        && v.getQtyRedeemed() != null
                        && v.getQtyRedeemed() >= v.getQtyTotal()) return false;
                    // audience type
                    String aud = v.getAudienceType();
                    if ("ALL".equalsIgnoreCase(aud)) return true;
                    if ("VIP".equalsIgnoreCase(aud)
                        && finalChosenCustomer != null
                        && segments
                            .getOrDefault("vip", Collections.emptyList())
                            .contains(finalChosenCustomer)) return true;
                    if ("NEW".equalsIgnoreCase(aud)
                        && finalChosenCustomer != null
                        && segments
                            .getOrDefault("newcomer", Collections.emptyList())
                            .contains(finalChosenCustomer)) return true;
                    return false;
                  })
              .collect(Collectors.toList());
      if (!applicable.isEmpty())
        chosenVoucher = applicable.get(ThreadLocalRandom.current().nextInt(applicable.size()));
    }

    // number of lines depends on segment, hour, holiday
    int numLines = sampleNumLinesAdvanced(chosenSegment, chosenStoreId);

    // get products available at store (batch items)
    List<BatchStock> storeBs =
        activeBatchStocks.stream()
            .filter(bs -> bs.getStoreId().equals(chosenStoreId))
            .collect(Collectors.toList());
    if (storeBs.isEmpty()) {
      System.out.println("Store has no active batch stocks, skip.");
      return;
    }
    Set<Long> batchIds = storeBs.stream().map(BatchStock::getBatchId).collect(Collectors.toSet());
    List<BatchItem> availableBatchItems =
        batchItemRepository.findAll().stream()
            .filter(bi -> bi != null && batchIds.contains(bi.getBatchId()))
            .collect(Collectors.toList());
    if (availableBatchItems.isEmpty()) {
      System.out.println("No available batch items for chosen store.");
      return;
    }

    Set<Long> availableProductIds =
        availableBatchItems.stream().map(BatchItem::getProductId).collect(Collectors.toSet());
    List<Product> availableProducts =
        productRepository.findAll().stream()
            .filter(p -> p != null && availableProductIds.contains(p.getId()))
            .collect(Collectors.toList());
    if (availableProducts.isEmpty()) {
      System.out.println("No available products in chosen store.");
      return;
    }

    // compute initial product scores (raw) using history + heuristics
    Map<Long, Double> rawScoreMap = computeRawScoresMap(availableProducts, chosenStoreId);

    // inventory mask & availability scaling (apply safety stock)
    for (Product p : availableProducts) {
      long avail = computeAvailableQtyForProductInStore(p.getId(), chosenStoreId);
      if (avail <= 0) rawScoreMap.put(p.getId(), 0.0);
      else {
        long effectiveAvail = Math.max(0, avail - (long) Math.ceil(avail * safetyStockPct));
        if (effectiveAvail <= 0) rawScoreMap.put(p.getId(), 0.0);
        else {
          double scale = Math.min(1.0, effectiveAvail / 5.0);
          rawScoreMap.put(
              p.getId(), rawScoreMap.getOrDefault(p.getId(), 0.0) * (0.5 + 0.5 * scale));
        }
      }
    }

    // convert raw scores map to arrays for softmax sampling
    double[] scores = new double[availableProducts.size()];
    for (int i = 0; i < availableProducts.size(); i++)
      scores[i] = rawScoreMap.getOrDefault(availableProducts.get(i).getId(), 0.0);

    // apply softmax with temperature (temperature adapt with hour)
    double temperature = adaptTemperatureByHour(nowLocal().getHour());
    double[] probs = softmax(scores, temperature);

    // sample basket: first choose main products without replacement
    List<SaleLineDto> lines = new ArrayList<>();
    List<Product> chosenProductsForLines = new ArrayList<>();

    boolean[] used = new boolean[availableProducts.size()];
    int attempts = 0;
    while (lines.size() < numLines && attempts < numLines * 12) {
      attempts++;
      Product chosenProduct = weightedChoiceProductWithMask(availableProducts, probs, used);
      if (chosenProduct == null) break;
      long availableQty =
          computeAvailableQtyForProductInStore(chosenProduct.getId(), chosenStoreId);
      if (availableQty <= 0) { // mark used and continue
        for (int i = 0; i < availableProducts.size(); i++)
          if (availableProducts.get(i).getId().equals(chosenProduct.getId())) used[i] = true;
        continue;
      }
      int qty = sampleQtyByCategoryImproved(chosenProduct, availableQty, chosenSegment);
      if (qty <= 0) { // mark used and continue
        for (int i = 0; i < availableProducts.size(); i++)
          if (availableProducts.get(i).getId().equals(chosenProduct.getId())) used[i] = true;
        continue;
      }

      // avoid duplicate
      boolean duplicate =
          lines.stream().anyMatch(l -> l.getProductId().equals(chosenProduct.getId()));
      if (duplicate) {
        for (int i = 0; i < availableProducts.size(); i++)
          if (availableProducts.get(i).getId().equals(chosenProduct.getId())) used[i] = true;
        continue;
      }

      SaleLineDto l = new SaleLineDto();
      l.setProductId(chosenProduct.getId());
      l.setQtyOrdered(qty);
      l.setUnitPrice(getUnitPriceSafe(chosenProduct));
      lines.add(l);
      chosenProductsForLines.add(chosenProduct);

      // mark chosen index used
      for (int i = 0; i < availableProducts.size(); i++)
        if (availableProducts.get(i).getId().equals(chosenProduct.getId())) used[i] = true;

      // companion logic (improved): use heuristics + top-sellers affinity approximation
      if (lines.size() < numLines
          && ThreadLocalRandom.current().nextDouble()
              < companionProbabilityBySegment(chosenSegment)) {
        Optional<Product> companion =
            findCompanionByCategoryAdvanced(
                availableProducts, chosenProduct, used, chosenStoreId, rawScoreMap);
        if (companion.isPresent()) {
          Product cpp = companion.get();
          long av2 = computeAvailableQtyForProductInStore(cpp.getId(), chosenStoreId);
          if (av2 > 0) {
            int q2 = sampleQtyByCategoryImproved(cpp, av2, chosenSegment);
            SaleLineDto l2 = new SaleLineDto();
            l2.setProductId(cpp.getId());
            l2.setQtyOrdered(q2);
            l2.setUnitPrice(getUnitPriceSafe(cpp));
            lines.add(l2);
            chosenProductsForLines.add(cpp);
            // mark used
            for (int i = 0; i < availableProducts.size(); i++)
              if (availableProducts.get(i).getId().equals(cpp.getId())) used[i] = true;
          }
        }
      }
    }

    if (lines.isEmpty()) {
      System.out.println("No lines created for order (maybe stock low).");
      return;
    }

    int preTotal = 0;
    for (int i = 0; i < lines.size(); i++)
      preTotal += lines.get(i).getUnitPrice() * lines.get(i).getQtyOrdered();
    if (chosenVoucher != null && !isVoucherSuitable(chosenVoucher, preTotal, chosenCustomer)) {
      chosenVoucher = null; // skip voucher if doesn't satisfy basic rules
    }

    // simulate cart abandonment
    if (enableCartAbandonment && shouldAbandonCart(chosenSegment, preTotal)) {
      System.out.println(
          "Simulated cart abandonment for a "
              + chosenSegment
              + " at store "
              + chosenStoreId
              + " estimated total "
              + preTotal);
      return; // don't save order
    }

    LocalDateTime now = nowLocal();
    SaleOrder order = new SaleOrder();
    order.setStoreId(chosenStoreId);
    order.setCustomerId(chosenCustomer == null ? null : chosenCustomer.getId());
    order.setPaymentId(chosenPayment == null ? null : chosenPayment.getId());
    order.setCreatorId(0L);
    order.setCreatedTime(now);
    order.setUpdaterID(0L);
    order.setUpdatedTime(now);
    if (chosenVoucher != null) order.setVoucherId(chosenVoucher.getId());

    SaleOrder savedOrder = saleOrderRepository.save(order);

    int finalPrice = 0;
    for (int i = 0; i < lines.size(); i++) {
      SaleLineDto dto = lines.get(i);
      Product p = chosenProductsForLines.get(i);
      SaleLine sl = new SaleLine();
      sl.setSaleOrderId(String.valueOf(savedOrder.getId()));
      sl.setProductId(dto.getProductId());
      sl.setQtyOrdered(dto.getQtyOrdered());
      sl.setUnitPrice(dto.getUnitPrice());
      sl.setCreatedTime(now);
      sl.setUpdatedTime(now);
      sl.setCreatorId(0L);
      sl.setUpdaterID(0L);
      SaleLine savedLine = saleLineRepository.save(sl);

      finalPrice += getUnitPriceSafe(p) * dto.getQtyOrdered();

      try {
        allocateInventoryForSaleLineHistorical(
            savedLine.getId(), p.getId(), dto.getQtyOrdered(), chosenStoreId, now);
      } catch (AppException ex) {
        throw new RuntimeException("Auto-generate allocation failed: " + ex.getMessage(), ex);
      }
    }

    if (chosenVoucher != null) {
      if (chosenVoucher.getDiscountPer() != null && chosenVoucher.getDiscountPer() > 0) {
        finalPrice = (int) Math.round(finalPrice * (1.0 - chosenVoucher.getDiscountPer() / 100.0));
      } else if (chosenVoucher.getDiscountVal() != null && chosenVoucher.getDiscountVal() > 0) {
        finalPrice = Math.max(0, finalPrice - chosenVoucher.getDiscountVal());
      }
    }

    savedOrder.setFinalPrice(finalPrice);
    saleOrderRepository.save(savedOrder);
    System.out.println(
        "Auto created order id = "
            + savedOrder.getId()
            + " store="
            + chosenStoreId
            + " lines="
            + lines.size()
            + " seg="
            + chosenSegment
            + " total="
            + finalPrice);
  }

  // ---------------- historical generator (unchanged except improvements) ----------------
  @Scheduled(cron = "${order.generator.daily-cron}")
  public void scheduledGenerateForNextWeek() {
    LocalDateTime startDate = nowLocal().plusDays(1);
    LocalDateTime endDate = startDate.plusWeeks(1);
    for (LocalDateTime date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
      generateOrdersForDate(date);
  }

  @Transactional
  public void generateOrdersForDate(LocalDateTime targetDate) {
    List<BatchStock> activeBatchStocks =
        batchStockRepository.findAll().stream()
            .filter(
                bs ->
                    bs != null
                        && bs.getStatus() != null
                        && "ACTIVE".equalsIgnoreCase(bs.getStatus()))
            .collect(Collectors.toList());
    if (activeBatchStocks.isEmpty()) {
      System.out.println("No active batch stocks -> skip daily generation.");
      return;
    }
    List<Long> stores =
        activeBatchStocks.stream()
            .map(BatchStock::getStoreId)
            .distinct()
            .collect(Collectors.toList());
    if (stores.isEmpty()) {
      System.out.println("No stores found -> skip.");
      return;
    }

    double[] hourWeights = new double[24];
    Arrays.fill(hourWeights, 0);
    hourWeights[7] = 4;
    hourWeights[8] = 5;
    hourWeights[9] = 4;
    hourWeights[15] = 3;
    hourWeights[16] = 3;
    hourWeights[20] = 5;
    hourWeights[21] = 5;
    hourWeights[22] = 4;
    double sumWeights = Arrays.stream(hourWeights).sum();
    if (sumWeights <= 0) {
      System.out.println("No hour weights set -> skip.");
      return;
    }

    int totalOrders = Math.max(1, this.dailyTotalOrders);
    int perStoreBase = totalOrders / stores.size();
    int remainder = totalOrders % stores.size();
    for (int si = 0; si < stores.size(); si++) {
      Long storeId = stores.get(si);
      int storeOrders = perStoreBase + (si < remainder ? 1 : 0);
      for (int o = 0; o < storeOrders; o++) {
        int hour = sampleHourByWeights(hourWeights);
        int minute = ThreadLocalRandom.current().nextInt(0, 60);
        int second = ThreadLocalRandom.current().nextInt(0, 60);
        LocalDateTime orderTime =
            LocalDateTime.of(LocalDate.from(targetDate), LocalTime.of(hour, minute, second));
        try {
          createHistoricalOrderForStoreAtTime(storeId, orderTime);
        } catch (Exception ex) {
          System.err.println(
              "Failed to create historical order for store "
                  + storeId
                  + " at "
                  + orderTime
                  + ": "
                  + ex.getMessage());
        }
      }
    }
  }

  @Transactional
  public void createHistoricalOrderForStoreAtTime(Long storeId, LocalDateTime orderTime)
      throws AppException {
    List<Customer> customers = customerRepository.findAll();
    Customer chosenCustomer = null;
    if (!customers.isEmpty() && ThreadLocalRandom.current().nextDouble() < 0.7)
      chosenCustomer = customers.get(ThreadLocalRandom.current().nextInt(customers.size()));

    List<PaymentMethod> payments = paymentMethodRepository.findAll();
    PaymentMethod chosenPayment =
        payments.isEmpty()
            ? null
            : payments.get(ThreadLocalRandom.current().nextInt(payments.size()));

    List<BatchStock> storeBs =
        batchStockRepository.findAll().stream()
            .filter(
                bs ->
                    bs != null
                        && bs.getStoreId() != null
                        && bs.getStoreId().equals(storeId)
                        && "ACTIVE".equalsIgnoreCase(bs.getStatus()))
            .collect(Collectors.toList());
    if (storeBs.isEmpty()) throw new AppException("No active batch stocks for store " + storeId);

    Set<Long> batchIds = storeBs.stream().map(BatchStock::getBatchId).collect(Collectors.toSet());
    List<BatchItem> availableBatchItems =
        batchItemRepository.findAll().stream()
            .filter(bi -> bi != null && batchIds.contains(bi.getBatchId()))
            .collect(Collectors.toList());
    if (availableBatchItems.isEmpty())
      throw new AppException("No batch items available for store " + storeId);

    Set<Long> productIds =
        availableBatchItems.stream().map(BatchItem::getProductId).collect(Collectors.toSet());
    List<Product> availableProducts =
        productRepository.findAll().stream()
            .filter(p -> p != null && productIds.contains(p.getId()))
            .collect(Collectors.toList());
    if (availableProducts.isEmpty()) throw new AppException("No products for store " + storeId);

    int numLines = sampleNumLines();
    SaleOrder order = new SaleOrder();
    order.setStoreId(storeId);
    order.setCustomerId(chosenCustomer == null ? null : chosenCustomer.getId());
    order.setPaymentId(chosenPayment == null ? null : chosenPayment.getId());
    order.setCreatorId(0L);
    order.setCreatedTime(orderTime);
    order.setUpdaterID(0L);
    order.setUpdatedTime(orderTime);
    int finalPrice = 0;
    SaleOrder savedOrder = saleOrderRepository.save(order);

    int attempts = 0;
    List<SaleLine> createdLines = new ArrayList<>();
    Map<Long, Double> rawScoreMap = computeRawScoresMap(availableProducts, storeId);
    for (int i = 0; i < availableProducts.size(); i++) {
      Product p = availableProducts.get(i);
      long avail = computeAvailableQtyForProductInStore(p.getId(), storeId);
      if (avail <= 0) rawScoreMap.put(p.getId(), 0.0);
      else
        rawScoreMap.put(
            p.getId(),
            rawScoreMap.getOrDefault(p.getId(), 0.0) * (0.5 + 0.5 * Math.min(1.0, avail / 5.0)));
    }

    double[] scores = new double[availableProducts.size()];
    for (int i = 0; i < availableProducts.size(); i++)
      scores[i] = rawScoreMap.getOrDefault(availableProducts.get(i).getId(), 0.0);
    double[] probs = softmax(scores, samplingTemperature);

    boolean[] used = new boolean[availableProducts.size()];
    while (createdLines.size() < numLines && attempts < numLines * 8) {
      attempts++;
      Product p = weightedChoiceProductWithMask(availableProducts, probs, used);
      if (p == null) break;
      long availQty = computeAvailableQtyForProductInStore(p.getId(), storeId);
      if (availQty <= 0) {
        for (int k = 0; k < availableProducts.size(); k++)
          if (availableProducts.get(k).getId().equals(p.getId())) used[k] = true;
        continue;
      }
      int qty = sampleQtyByCategoryImproved(p, availQty, "regular");
      if (qty <= 0) {
        for (int k = 0; k < availableProducts.size(); k++)
          if (availableProducts.get(k).getId().equals(p.getId())) used[k] = true;
        continue;
      }
      boolean dup = createdLines.stream().anyMatch(l -> l.getProductId().equals(p.getId()));
      if (dup) {
        for (int k = 0; k < availableProducts.size(); k++)
          if (availableProducts.get(k).getId().equals(p.getId())) used[k] = true;
        continue;
      }

      SaleLine line = new SaleLine();
      line.setSaleOrderId(String.valueOf(savedOrder.getId()));
      line.setProductId(p.getId());
      line.setQtyOrdered(qty);
      line.setUnitPrice(getUnitPriceSafe(p));
      line.setCreatedTime(orderTime);
      line.setUpdatedTime(orderTime);
      line.setCreatorId(0L);
      line.setUpdaterID(0L);
      SaleLine savedLine = saleLineRepository.save(line);
      createdLines.add(savedLine);
      finalPrice += getUnitPriceSafe(p) * qty;
      allocateInventoryForSaleLineHistorical(savedLine.getId(), p.getId(), qty, storeId, orderTime);
    }

    savedOrder.setFinalPrice(finalPrice);
    saleOrderRepository.save(savedOrder);
    System.out.println(
        "Generated historical order id="
            + savedOrder.getId()
            + " time="
            + orderTime
            + " lines="
            + createdLines.size());
  }

  // allocation unchanged but respects safety stock (already used in
  // computeAvailableQtyForProductInStore)
  private void allocateInventoryForSaleLineHistorical(
      Long saleLineId, Long productId, Integer qtyNeeded, Long storeId, LocalDateTime orderTime)
      throws AppException {
    List<BatchItem> availableBatchItems =
        batchItemRepository.findAvailableByProductAndStoreOrdered(productId, storeId);
    int remaining = qtyNeeded;
    for (BatchItem bi : availableBatchItems) {
      if (remaining <= 0) break;
      int original = bi.getOriginalQty() == null ? 0 : bi.getOriginalQty();
      int sold =
          saleAllocationService.getTotalSoldQuantityByBatchItem(bi.getId()) == null
              ? 0
              : saleAllocationService.getTotalSoldQuantityByBatchItem(bi.getId());
      int avail = Math.max(0, original - (sold));
      // apply safety stock per batch item
      int safeKeep = (int) Math.ceil(original * safetyStockPct);
      avail = Math.max(0, avail - safeKeep);
      if (avail <= 0) continue;
      int allocateQty = Math.min(remaining, avail);

      Optional<BatchStock> bsOpt =
          batchStockRepository.findFirstByBatchIdAndStoreId(bi.getBatchId(), storeId);
      Long batchStockId = bsOpt.map(BatchStock::getId).orElse(null);
      if (batchStockId == null) {
        bsOpt =
            batchStockRepository.findAll().stream()
                .filter(
                    bs ->
                        bs != null
                            && bs.getBatchId() != null
                            && bs.getBatchId().equals(bi.getBatchId()))
                .findFirst();
        if (bsOpt.isPresent()) batchStockId = bsOpt.get().getId();
      }
      if (batchStockId == null)
        throw new AppException(
            "batchStockId not found for batchItem "
                + bi.getId()
                + " (batchId="
                + bi.getBatchId()
                + ", storeId="
                + storeId
                + ")");

      SaleAllocation alloc = new SaleAllocation();
      alloc.setSaleLineId(saleLineId);
      alloc.setBatchItemId(bi.getId());
      alloc.setBatchStockId(batchStockId);
      alloc.setSoldQty(allocateQty);
      alloc.setUnitCostSnap(bi.getImportPrice());
      alloc.setCreatedTime(orderTime);
      alloc.setCreatorId(0L);
      alloc.setUpdatedTime(orderTime);
      alloc.setUpdaterID(0L);
      saleAllocationRepository.save(alloc);
      remaining -= allocateQty;
    }
    if (remaining > 0)
      throw new AppException(
          "Unable to allocate full qty for product " + productId + ", missing " + remaining);
  }

  // ---------- helpers (shared) ----------

  private Map<String, List<Customer>> classifyCustomersAdvanced(List<Customer> customers) {
    Map<String, List<Customer>> m = new HashMap<>();
    m.put("vip", new ArrayList<>());
    m.put("regular", new ArrayList<>());
    m.put("newcomer", new ArrayList<>());

    for (Customer c : customers) {
      if (c == null) continue;
      int point = c.getPoint() == null ? 0 : c.getPoint();
      int recencyDays = sampleSyntheticRecencyByPoints(point);
      double freq = sampleSyntheticFrequencyByPoints(point);
      double score =
          normalizePoints(point) * 0.5
              + recencyScore(recencyDays) * 0.3
              + Math.min(1.0, freq / 5.0) * 0.2;

      if (score > 0.75) m.get("vip").add(c);
      else if (point < 60 && recencyDays > 180) m.get("newcomer").add(c);
      else m.get("regular").add(c);
    }
    return m;
  }

  private int sampleSyntheticRecencyByPoints(int point) {
    if (point >= 1000) return ThreadLocalRandom.current().nextInt(0, 30);
    if (point >= 300) return ThreadLocalRandom.current().nextInt(10, 90);
    if (point >= 100) return ThreadLocalRandom.current().nextInt(30, 180);
    return ThreadLocalRandom.current().nextInt(60, 720);
  }

  private double sampleSyntheticFrequencyByPoints(int point) {
    if (point >= 1000) return 6 + ThreadLocalRandom.current().nextDouble() * 6; // many orders
    if (point >= 300) return 2 + ThreadLocalRandom.current().nextDouble() * 4;
    if (point >= 100) return 1 + ThreadLocalRandom.current().nextDouble() * 3;
    return ThreadLocalRandom.current().nextDouble() * 1.5;
  }

  private double normalizePoints(int point) {
    return Math.tanh(point / 1500.0);
  }

  private double recencyScore(int days) {
    return Math.exp(-days / 90.0);
  }

  private int sampleNumLinesAdvanced(String segment, Long storeId) {
    LocalDateTime today = nowLocal();
    boolean holiday =
        simpleHolidays.contains(
            nowLocal().toLocalDate().getMonth() == null ? null : MonthDay.from(nowLocal()));
    int hour = nowLocal().getHour();
    double baseLambda;
    if ("vip".equals(segment)) baseLambda = 3.5;
    else if ("regular".equals(segment)) baseLambda = 2.0;
    else if ("newcomer".equals(segment)) baseLambda = 1.5;
    else baseLambda = 1.2; // guest

    // lunch/dinner bump
    if (hour >= 11 && hour <= 14) baseLambda *= 1.1;
    if (hour >= 17 && hour <= 21) baseLambda *= 1.25;
    if (holiday) baseLambda *= 1.6;

    int sampled = samplePoisson(Math.max(0.5, baseLambda));
    // clamp
    sampled = Math.max(1, Math.min(8, sampled));
    return sampled;
  }

  // Poisson sampler (Knuth)
  private int samplePoisson(double lambda) {
    double L = Math.exp(-lambda);
    int k = 0;
    double p = 1.0;
    while (p > L) {
      k++;
      p *= ThreadLocalRandom.current().nextDouble();
      if (k > 100) break;
    }
    return Math.max(1, k - 1);
  }

  private double adaptTemperatureByHour(int hour) {
    if (hour >= 11 && hour <= 13) return 0.8; // focused popular during lunch
    if (hour >= 17 && hour <= 20) return 0.9;
    return 1.1; // exploratory off-peak
  }

  private double companionProbabilityBySegment(String seg) {
    switch (seg) {
      case "vip":
        return 0.45;
      case "regular":
        return 0.3;
      case "newcomer":
        return 0.15;
      default:
        return 0.08;
    }
  }

  private Optional<Product> findCompanionByCategoryAdvanced(
      List<Product> products,
      Product chosen,
      boolean[] used,
      Long storeId,
      Map<Long, Double> rawScoreMap) {
    // prefer complementary: if chosen is snack -> look for drink; if chosen is coffee -> look for
    // milk/cream/sugar
    String chosenText =
        (chosen.getName() == null ? "" : chosen.getName()).toLowerCase()
            + " "
            + (chosen.getDescription() == null ? "" : chosen.getDescription()).toLowerCase();
    boolean preferDrink =
        chosenText.contains("snack") || chosenText.contains("bánh") || chosenText.contains("chips");
    List<Integer> candidateIndices = new ArrayList<>();
    for (int i = 0; i < products.size(); i++) {
      if (used[i]) continue;
      Product p = products.get(i);
      String t =
          (p.getName() == null ? "" : p.getName()).toLowerCase()
              + " "
              + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();
      if (preferDrink
          && (t.contains("nước")
              || t.contains("cola")
              || t.contains("juice")
              || t.contains("nước ngọt"))) candidateIndices.add(i);
    }
    // if none found, fallback to top rawScore products (but not the same)
    if (candidateIndices.isEmpty()) {
      List<Integer> indices = new ArrayList<>();
      for (int i = 0; i < products.size(); i++)
        if (!used[i] && !products.get(i).getId().equals(chosen.getId())) indices.add(i);
      indices.sort(
          (a, b) ->
              Double.compare(
                  rawScoreMap.getOrDefault(products.get(b).getId(), 0.0),
                  rawScoreMap.getOrDefault(products.get(a).getId(), 0.0)));
      if (!indices.isEmpty()) candidateIndices.add(indices.get(0));
    }
    if (candidateIndices.isEmpty()) return Optional.empty();
    int chosenIdx =
        candidateIndices.get(ThreadLocalRandom.current().nextInt(candidateIndices.size()));
    return Optional.of(products.get(chosenIdx));
  }

  private Map<Long, Double> computeRawScoresMap(List<Product> products, Long storeId) {
    Map<Long, Double> map = new HashMap<>();
    try {
      LocalDateTime to = nowLocal();
      LocalDateTime from = to.minusDays(60);
      int numberOfProducts = Math.max(50, products.size());
      List<Map<String, Object>> topSold =
          saleLineRepository.findMostSoldProductsOfIntervalWithQtyOfAStore(
              from, to, numberOfProducts, storeId);
      Map<Long, Long> qtyMap = new HashMap<>();
      if (topSold != null) {
        for (Map<String, Object> r : topSold) {
          try {
            Object idObj = r.get("id");
            Object qtyObj = r.get("totalQuantitySold");
            Long id =
                idObj == null
                    ? null
                    : (idObj instanceof Number
                        ? ((Number) idObj).longValue()
                        : Long.parseLong(idObj.toString()));
            Long qty =
                qtyObj == null
                    ? 0L
                    : (qtyObj instanceof Number
                        ? ((Number) qtyObj).longValue()
                        : Long.parseLong(qtyObj.toString()));
            if (id != null) qtyMap.put(id, qty);
          } catch (Exception e) {
          }
        }
      }

      int hour = nowLocal().getHour();
      int month = nowLocal().getMonthValue();
      DayOfWeek dow = nowLocal().getDayOfWeek();

      for (Product p : products) {
        if (p == null) continue;
        long historicalQty = qtyMap.getOrDefault(p.getId(), 0L);
        double base = 1.0 + Math.log(1.0 + historicalQty);
        int up = getUnitPriceSafe(p);
        double priceFactor = 1.0;
        if (up < 10000) priceFactor = 1.6;
        else if (up < 30000) priceFactor = 1.3;
        else if (up < 100000) priceFactor = 1.0;
        else priceFactor = 0.6;
        String text =
            (p.getName() == null ? "" : p.getName()).toLowerCase()
                + " "
                + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();
        double catFactor = 1.0;
        String key = "other";
        if (text.contains("nước")
            || text.contains("juice")
            || text.contains("cola")
            || text.contains("nước ngọt")
            || text.contains("water")) {
          catFactor = 1.6;
          key = "drink";
        } else if (text.contains("snack")
            || text.contains("bánh")
            || text.contains("chips")
            || text.contains("kẹo")) {
          catFactor = 1.4;
          key = "snack";
        } else if (text.contains("trái")
            || text.contains("hoa quả")
            || text.contains("cam")
            || text.contains("táo")) {
          catFactor = 1.0;
          key = "fruit";
        } else if (text.contains("cà phê") || text.contains("coffee")) {
          catFactor = 1.2;
          key = "coffee";
        }

        double hourBoost = 1.0;
        if (hourBoosts.containsKey(key)) {
          hourBoost = hourBoosts.get(key)[hour];
        }
        // weekend boost for snacks
        if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) && key.equals("snack"))
          hourBoost *= dowBoosts.getOrDefault("weekend_snack", 1.0);

        double season = 1.0;
        if (seasonalMultipliers.containsKey(key)) {
          double[] arr = seasonalMultipliers.get(key);
          season = arr[Math.max(0, Math.min(11, month - 1))];
        }

        double weight = base * priceFactor * catFactor * hourBoost * season;
        map.put(p.getId(), Math.max(0.01, weight));
      }

      // normalize into probabilities-like raw scores
      double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();
      if (sum > 0) {
        for (Map.Entry<Long, Double> e : map.entrySet()) e.setValue(e.getValue() / sum);
      }

    } catch (Exception ex) {
      for (Product p : products) {
        if (p == null) continue;
        map.put(p.getId(), 1.0);
      }
    }
    return map;
  }

  private double[] softmax(double[] scores, double temperature) {
    double max = Double.NEGATIVE_INFINITY;
    for (double s : scores) if (s > max) max = s;
    double[] exps = new double[scores.length];
    double sum = 0.0;
    double temp = Math.max(1e-6, temperature);
    for (int i = 0; i < scores.length; i++) {
      double v = Math.exp((scores[i] - max) / temp);
      exps[i] = v;
      sum += v;
    }
    if (sum <= 0) {
      double[] fallback = new double[scores.length];
      Arrays.fill(fallback, 1.0 / scores.length);
      return fallback;
    }
    for (int i = 0; i < scores.length; i++) exps[i] /= sum;
    return exps;
  }

  private Product weightedChoiceProductWithMask(
      List<Product> products, double[] probs, boolean[] used) {
    if (products == null || products.isEmpty()) return null;
    double total = 0.0;
    for (int i = 0; i < products.size(); i++) if (!used[i]) total += probs[i];
    if (total <= 0) return null;
    double r = ThreadLocalRandom.current().nextDouble() * total;
    double acc = 0.0;
    for (int i = 0; i < products.size(); i++) {
      if (used[i]) continue;
      acc += probs[i];
      if (r <= acc) return products.get(i);
    }
    // fallback
    for (int i = 0; i < products.size(); i++) if (!used[i]) return products.get(i);
    return null;
  }

  private PaymentMethod weightedChoicePayment(List<PaymentMethod> payments, double[] baseWeights) {
    if (payments == null || payments.isEmpty()) return null;
    double[] weights = new double[payments.size()];
    if (baseWeights == null || baseWeights.length == 0) Arrays.fill(weights, 1.0);
    else {
      for (int i = 0; i < payments.size(); i++) {
        if (i < baseWeights.length) weights[i] = Math.max(0.0, baseWeights[i]);
        else weights[i] = baseWeights[baseWeights.length - 1];
      }
    }
    double total = Arrays.stream(weights).sum();
    if (total <= 0) return payments.get(ThreadLocalRandom.current().nextInt(payments.size()));
    double r = ThreadLocalRandom.current().nextDouble() * total;
    double acc = 0;
    for (int i = 0; i < payments.size(); i++) {
      acc += weights[i];
      if (r <= acc) return payments.get(i);
    }
    return payments.get(payments.size() - 1);
  }

  private Long weightedChoiceLong(List<Long> items, double[] weights) {
    if (items == null || items.isEmpty()) return null;
    double total = Arrays.stream(weights).sum();
    double r = ThreadLocalRandom.current().nextDouble() * total;
    double acc = 0;
    for (int i = 0; i < items.size(); i++) {
      acc += weights[i];
      if (r <= acc) return items.get(i);
    }
    return items.get(items.size() - 1);
  }

  private int sampleNumLines() {
    int[] options = {1, 1, 1, 2, 2, 3, 3, 4};
    return options[ThreadLocalRandom.current().nextInt(options.length)];
  }

  private int sampleQtyByCategory(Product p, long available) {
    // fallback kept for compatibility
    if (p == null) return 1;
    if (getUnitPriceSafe(p) > 1_000_000) return 1;
    double val = 1.5 + ThreadLocalRandom.current().nextGaussian() * 1.2;
    int qty = Math.max(1, (int) Math.round(val));
    qty = Math.min(qty, (int) Math.max(1, Math.min(available, 10)));
    return qty;
  }

  private int sampleQtyByCategoryImproved(Product p, long available, String segment) {
    if (p == null) return 1;
    int up = getUnitPriceSafe(p);
    String text =
        (p.getName() == null ? "" : p.getName()).toLowerCase()
            + " "
            + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();

    // packs
    List<Integer> packSizes = new ArrayList<>();
    if (text.contains("6-pack") || text.contains("6 lon") || text.contains("6pack"))
      packSizes.add(6);
    if (text.contains("12-pack") || text.contains("12 lon") || text.contains("12pack"))
      packSizes.add(12);
    if (text.contains("lốc 6") || text.contains("lốc")) packSizes.add(6);

    // beverage behavior
    if (text.contains("nước")
        || text.contains("cola")
        || text.contains("juice")
        || text.contains("water")) {
      // common purchases: 1,2,6,12 (pack)
      int[] options = {1, 1, 2, 2, 2, 6};
      int pick = options[ThreadLocalRandom.current().nextInt(options.length)];
      if (!packSizes.isEmpty() && ThreadLocalRandom.current().nextDouble() < 0.12)
        pick = packSizes.get(ThreadLocalRandom.current().nextInt(packSizes.size()));
      pick = Math.min((int) available, pick);
      if (segment.equals("vip"))
        pick =
            Math.min(
                (int) Math.max(1, pick + ThreadLocalRandom.current().nextInt(0, 2)),
                (int) available);
      return Math.max(1, pick);
    }

    // snacks: small multiples
    if (text.contains("snack")
        || text.contains("bánh")
        || text.contains("chips")
        || text.contains("kẹo")) {
      int[] opts = {1, 1, 2, 2, 3};
      int pick = opts[ThreadLocalRandom.current().nextInt(opts.length)];
      if (segment.equals("vip")) pick += ThreadLocalRandom.current().nextInt(0, 2);
      pick = Math.min(pick, (int) available);
      return Math.max(1, pick);
    }

    // high price items -> qty 1 almost always
    if (up > 200_000) return 1;

    // cheap items -> allow Poisson-ish
    if (up < 10000) {
      int pick = samplePoisson(2.0);
      pick = Math.min((int) available, Math.max(1, pick));
      return pick;
    }

    // default
    return Math.max(1, Math.min((int) available, 1 + ThreadLocalRandom.current().nextInt(0, 3)));
  }

  private long computeAvailableQtyForProductInStore(Long productId, Long storeId) {
    if (productId == null || storeId == null) return 0;
    Long total = batchItemRepository.getTotalAvailableQtyForProductInStore(productId, storeId);
    if (total == null) return 0L;
    return total;
  }

  private int sampleHourByWeights(double[] weights) {
    double total = Arrays.stream(weights).sum();
    double r = ThreadLocalRandom.current().nextDouble() * total;
    double acc = 0;
    for (int i = 0; i < weights.length; i++) {
      acc += weights[i];
      if (r <= acc) return i;
    }
    return weights.length - 1;
  }

  // safe getter for unit price
  private int getUnitPriceSafe(Product p) {
    if (p == null) return 0;
    try {
      Integer up = p.getUnitPrice();
      return up == null ? 0 : up;
    } catch (Throwable t) {
      return 0;
    }
  }

  // small helpers
  private <T> T randomFromList(List<T> list) {
    return list == null || list.isEmpty()
        ? null
        : list.get(ThreadLocalRandom.current().nextInt(list.size()));
  }

  // voucher suitability basic checks
  private boolean isVoucherSuitable(Voucher v, int orderTotal, Customer c) {
    if (v == null) return false;
    if (v.getQtyTotal() != null
        && v.getQtyRedeemed() != null
        && v.getQtyRedeemed() >= v.getQtyTotal()) return false;
    // example: if discountVal indicates some threshold logic (not present in entity), skip. This is
    // placeholder.
    if (v.getPerCustomerLimit() != null && v.getPerCustomerLimit() <= 0) return false;
    // if voucher requires minimum order (not in entity) we can't check - assume ok
    return true;
  }

  // cart abandonment heuristic
  private boolean shouldAbandonCart(String segment, int preTotal) {
    if (!enableCartAbandonment) return false;
    double base = 0.35; // baseline abandonment
    if ("vip".equals(segment)) base -= 0.15;
    if ("guest".equals(segment)) base += 0.15;
    if (preTotal > 500_000) base += 0.05;
    return ThreadLocalRandom.current().nextDouble() < base;
  }

  // dùng cho test
  public void generateOrdersForDateWithTotal(LocalDateTime targetDate, int totalOrders) {
    int prev = this.dailyTotalOrders;
    try {
      this.dailyTotalOrders = Math.max(1, totalOrders);
      generateOrdersForDate(targetDate);
    } finally {
      this.dailyTotalOrders = prev;
    }
  }

  public void generateOrdersForTomorrow() {
    generateOrdersForDate(nowLocal().plusDays(1));
  }
}
