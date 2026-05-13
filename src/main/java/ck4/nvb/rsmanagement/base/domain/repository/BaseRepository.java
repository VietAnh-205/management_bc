package ck4.nvb.rsmanagement.base.domain.repository;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends IEntity<ID>, ID extends Comparable<ID> & Serializable>
    extends JpaRepository<T, ID>, QuerydslPredicateExecutor<T> {}
