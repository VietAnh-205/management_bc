package ck4.nvb.rsmanagement.core.module.order.customer.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.CustomerRepository;
import ck4.nvb.rsmanagement.core.module.order.customer.service.dto.CustomerDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("customerService")
public class CustomerCrudServiceImpl
    extends FullAuditedCrudServiceImpl<CustomerDto, Customer, Long, UserGetDto, Long> {

  protected CustomerCrudServiceImpl(CustomerRepository repository) {
    super(repository, Customer.class);
  }

  @Override
  public CustomerRepository getRepository() {
    return (CustomerRepository) super.getRepository();
  }

  @Override
  public CustomerDto mapToEntityDto(Customer entity) {
    return new ModelMapper().map(entity, CustomerDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("name", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("phone", List.of(SearchOperator.EQUALS));
    keys.put("gender", List.of(SearchOperator.EQUALS));
    keys.put(
        "point",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.LESS_THAN,
            SearchOperator.LESS_THAN_OR_EQUAL,
            SearchOperator.GREATER_THAN,
            SearchOperator.GREATER_THAN_OR_EQUAL));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("name");
    keys.add("phone");
    keys.add("gender");
    keys.add("point");
    return keys;
  }
}
