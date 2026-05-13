package ck4.nvb.rsmanagement.core.module.order.report;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.service.ProductServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/reports/weeks")
public class WeeklySalesReportController {

    @Autowired private WeeklySalesAggregateService aggregateService;

    @Autowired private ProductServiceImpl productService;

    @GetMapping("/{weekStart}/stores/{storeId}/top")
    public ResponseEntity<List<TopProductDto>> topProducts(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @PathVariable Long storeId,
            @RequestParam(name = "n", defaultValue = "10") int n) {
        List<ProductWeeklySales> rows = aggregateService.topN(storeId, weekStart, n);
        List<TopProductDto> out =
                rows.stream()
                        .map(
                                r -> {
                                    var p = productService.getEntity(r.getProductId());
                                    return new TopProductDto(
                                            r.getProductId(), p.getSku(), p.getName(), r.getQty(), r.getRevenue());
                                })
                        .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{weekStart}/stores/{storeId}/bottom")
    public ResponseEntity<List<TopProductDto>> bottomProducts(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @PathVariable Long storeId,
            @RequestParam(name = "n", defaultValue = "10") int n) {
        List<ProductWeeklySales> rows = aggregateService.bottomN(storeId, weekStart, n);
        List<TopProductDto> out =
                rows.stream()
                        .map(
                                r -> {
                                    var p = productService.getEntity(r.getProductId());
                                    return new TopProductDto(
                                            r.getProductId(), p.getSku(), p.getName(), r.getQty(), r.getRevenue());
                                })
                        .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{weekStart}/stores/{storeId}/cold")
    public ResponseEntity<List<ProductGetDto>> coldProducts(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @PathVariable Long storeId,
            @RequestParam(name = "n", defaultValue = "50") int n) {

        // product ids đã bán trong tuần
        List<Long> sold = aggregateService.productIdsSoldInWeek(storeId, weekStart);
        Set<Long> soldSet = new HashSet<>(sold); // để lookup O(1)

        // tất cả product ids của store (nhẹ)
        List<Long> allIds = productService.getAllProductIdsOfAStore(storeId);

        // lấy cold ids (không xuất hiện trong sold), limit n
        List<Long> coldIds =
                allIds.stream().filter(id -> !soldSet.contains(id)).limit(n).collect(Collectors.toList());

        // lấy entity Product cho coldIds và map sang DTO
        List<Product> products = productService.getRepository().findAllById(coldIds);
        List<ProductGetDto> out = products.stream().map(productService::mapToEntityDto).collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }
}
