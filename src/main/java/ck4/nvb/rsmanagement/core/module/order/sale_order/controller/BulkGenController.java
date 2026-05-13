package ck4.nvb.rsmanagement.core.module.order.sale_order.controller;

import ck4.nvb.rsmanagement.core.module.order.sale_order.service.BulkSaleOrderGenerator;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/bulk")
public class BulkGenController {
  private final BulkSaleOrderGenerator generator;

  public BulkGenController(BulkSaleOrderGenerator generator) {
    this.generator = generator;
  }

  @PostMapping("/generate")
  public ResponseEntity<String> generate(
      @RequestParam String start,
      @RequestParam String end,
      @RequestParam(defaultValue = "800") int batchSize,
      @RequestParam(defaultValue = "4") int threads) {
    LocalDate s = LocalDate.parse(start);
    LocalDate e = LocalDate.parse(end);

    new Thread(
            () -> {
              try {
                generator.generateRange(s, e, batchSize, threads);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            })
        .start();

    return ResponseEntity.ok(
        "Started generation from "
            + start
            + " to "
            + end
            + " (batchSize="
            + batchSize
            + ", threads="
            + threads
            + ")");
  }
}
