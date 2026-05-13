package ck4.nvb.rsmanagement.core.module.stores.category.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.category.domain.Category;
import ck4.nvb.rsmanagement.core.module.stores.category.domain.CategoryRepository;
import ck4.nvb.rsmanagement.core.module.stores.category.service.dto.CategoryDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("categoryService")
public class CategoryServiceImpl
    extends FullAuditedCrudServiceImpl<CategoryDto, Category, Long, UserGetDto, Long> {

  protected CategoryServiceImpl(CategoryRepository repository) {
    super(repository, Category.class);
  }

  @Override
  public CategoryRepository getRepository() {
    return (CategoryRepository) super.getRepository();
  }

  @Override
  public CategoryDto mapToEntityDto(Category entity) {
    return new ModelMapper().map(entity, CategoryDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("name", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("name");
    return keys;
  }
}
