package ck4.nvb.rsmanagement.core.module.order.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class WeeklySalesAggregateService {

    private final JdbcTemplate jdbc;
    private final ProductWeeklySalesRepository repository;
    private final ProductWeeklySalesAuditRepository auditRepository;

    @Autowired
    public WeeklySalesAggregateService(JdbcTemplate jdbc, ProductWeeklySalesRepository repository, ProductWeeklySalesAuditRepository auditRepository) {
        this.jdbc = jdbc;
        this.repository = repository;
        this.auditRepository = auditRepository;
    }

    private LocalDate toWeekStart(LocalDateTime dt) {
        return dt.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate toWeekStart(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @Transactional
    public void increment(Long storeId, Long productId, LocalDateTime orderTime, long qty, long revenue, String changeType, String referenceId) {
        LocalDate weekStart = toWeekStart(orderTime);
        String sql = "INSERT INTO product_weekly_sales(store_id, product_id, week_start, qty, revenue, last_updated) VALUES (?, ?, ?, ?, ?, now())"
                + " ON CONFLICT (store_id, product_id, week_start) DO UPDATE SET qty = product_weekly_sales.qty + EXCLUDED.qty, revenue = product_weekly_sales.revenue + EXCLUDED.revenue, last_updated = now();";
        jdbc.update(sql, storeId, productId, Date.valueOf(weekStart), qty, revenue);

        // write audit
        ProductWeeklySalesAudit a = new ProductWeeklySalesAudit();
        a.setStoreId(storeId);
        a.setProductId(productId);
        a.setWeekStart(weekStart);
        a.setDeltaQty(qty);
        a.setDeltaRevenue(revenue);
        a.setChangeType(changeType);
        a.setReferenceId(referenceId);
        auditRepository.save(a);
    }

    @Transactional
    public void decrement(Long storeId, Long productId, LocalDateTime orderTime, long qty, long revenue, String changeType, String referenceId) {
        LocalDate weekStart = toWeekStart(orderTime);
        String sql = "UPDATE product_weekly_sales SET qty = qty - ?, revenue = revenue - ?, last_updated = now() WHERE store_id = ? AND product_id = ? AND week_start = ?";
        jdbc.update(sql, qty, revenue, storeId, productId, Date.valueOf(weekStart));

        // audit
        ProductWeeklySalesAudit a = new ProductWeeklySalesAudit();
        a.setStoreId(storeId);
        a.setProductId(productId);
        a.setWeekStart(weekStart);
        a.setDeltaQty(-qty);
        a.setDeltaRevenue(-revenue);
        a.setChangeType(changeType);
        a.setReferenceId(referenceId);
        auditRepository.save(a);
    }

    @Transactional
    public void backfill(LocalDate fromInclusive, LocalDate toInclusive) {
        // compute monday boundaries
        LocalDate fromWeek = toWeekStart(fromInclusive);
        LocalDate toWeek = toWeekStart(toInclusive);

        String sql = "INSERT INTO product_weekly_sales(store_id, product_id, week_start, qty, revenue, created_at, last_updated) "
                + "SELECT so.store_id, sl.product_id, date_trunc('week', so.created_at)::date AS week_start, SUM(sl.qty_ordered) AS qty, SUM(sl.qty_ordered * sl.unit_price) AS revenue, now(), now() "
                + "FROM sale_line sl JOIN sale_order so ON sl.sale_order_id = so.id "
                + "WHERE so.created_at >= ? AND so.created_at < ? "
                + "GROUP BY so.store_id, sl.product_id, week_start "
                + "ON CONFLICT (store_id, product_id, week_start) DO UPDATE SET qty = EXCLUDED.qty, revenue = EXCLUDED.revenue, last_updated = now();";

        // toInclusive -> exclusive bound for created_at: toInclusive + 1 day
        LocalDateTime fromTs = fromWeek.atStartOfDay();
        LocalDateTime toTs = toWeek.plusDays(7).atStartOfDay();

        jdbc.update(sql, java.sql.Timestamp.valueOf(fromTs), java.sql.Timestamp.valueOf(toTs));
    }

    // helper queries for controller
    public List<ProductWeeklySales> topN(Long storeId, LocalDate weekStart, int n) {
        return repository.findByStoreIdAndWeekStartOrderByQtyDesc(storeId, weekStart).stream().limit(n).toList();
    }

    public List<ProductWeeklySales> bottomN(Long storeId, LocalDate weekStart, int n) {
        return repository.findByStoreIdAndWeekStartOrderByQtyAsc(storeId, weekStart).stream().limit(n).toList();
    }

    public List<Long> productIdsSoldInWeek(Long storeId, LocalDate weekStart) {
        return repository.findProductIdsSoldInWeek(storeId, weekStart);
    }
}