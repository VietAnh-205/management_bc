package ck4.nvb.rsmanagement.base.application.dto.caching;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import java.io.Serializable;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public abstract class EntityCache<
    CachedItem extends EntityDto<ID>, ID extends Comparable<ID> & Serializable> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public abstract String getCacheName();

  public CachedItem get(ID id) {
    getLogger().info("[CACHE] {} - Not found cached entity with id {}", getCacheName(), id);
    return null;
  }

  public CachedItem put(ID id, CachedItem cachedItem) {
    getLogger()
        .info("[CACHE] {} - Put entity with id {} to cache: {}", getCacheName(), id, cachedItem);
    return cachedItem;
  }

  public void remove(ID id) {
    getLogger().info("[CACHE] {} - Remove entity with id {}", getCacheName(), id);
  }

  public void clear() {
    getLogger().info("[CACHE] {} - Clear all cached entities", getCacheName());
  }
}
