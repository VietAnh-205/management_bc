package ck4.nvb.rsmanagement.core.module.users.user.service.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class BaseDTO {
  Long id;
  LocalDateTime createdTime;
  Long creatorId;
  LocalDateTime updatedTime;
  Long updaterId;
}
