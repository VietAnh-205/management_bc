package ck4.nvb.rsmanagement.core.module.stores.category.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.category.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDto extends EntityDto<Long>
    implements CreateInput<Category>, UpdateInput<Category> {

  private String name;

  private String description;

  @Override
  public Category mapToEntity() {
    Category category = new Category();
    category.setName(name);
    category.setDescription(description);
    return category;
  }

  @Override
  public boolean mapToEntity(Category entity) {
    boolean isModified = false;

    if (!name.equals(entity.getName())) {
      entity.setName(name);
      isModified = true;
    }

    if (!description.equals(entity.getDescription())) {
      entity.setDescription(description);
      isModified = true;
    }
    return isModified;
  }
}
