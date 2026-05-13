package ck4.nvb.rsmanagement.core.module.dashboard;

import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.CustomerRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrderRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("dashboardService")
@RequiredArgsConstructor
@Getter
public class DashboardServiceImpl implements DashboardService {
  private final ProductRepository productRepository;
  private final SaleOrderRepository orderRepository;
  private final CustomerRepository customerRepository;

  @Override
  public DashboardSummaryDto getDashboardSummary() throws AppException {
    int totalProducts = getProductRepository().countProductsByDeletedIsFalse();

    int totalOrders =
        getOrderRepository()
            .countOrdersByCreatedTimeBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
    long monthlyRevenue =
        Optional.ofNullable(
                getOrderRepository()
                    .sumTotalFinalPriceBetween(
                        LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                        LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay()))
            .orElse(0L);

    int totalCustomers = getCustomerRepository().countCustomerByDeletedIsFalse();

    return new DashboardSummaryDto(totalProducts, totalOrders, monthlyRevenue, totalCustomers);
  }
}
