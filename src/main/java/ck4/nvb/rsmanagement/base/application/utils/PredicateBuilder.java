package ck4.nvb.rsmanagement.base.application.utils;

import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.util.*;
import org.springframework.util.CollectionUtils;

public class PredicateBuilder<T> {
  private final Class<T> t;

  private final String entityVariable;

  private final List<SearchCriteria> criterias;

  private Map<String, String> replaceKeyMap;

  /**
   * instantiates a new common predicate builder.
   *
   * @param t the t
   */
  public PredicateBuilder(Class<T> t) {
    this.t = t;
    this.entityVariable = getEntityVariable(t.getSimpleName());
    this.criterias = new ArrayList<>();
    this.replaceKeyMap = new HashMap<>();
  }

  /**
   * replace key map.
   *
   * @param replaceKeyMap the replacement key map
   * @return the common predicate builder
   */
  public PredicateBuilder<T> replaceKeyMap(Map<String, String> replaceKeyMap) {
    this.replaceKeyMap = replaceKeyMap;
    return this;
  }

  /**
   * @param criteria the criteria
   * @return the common predicate builder
   */
  public PredicateBuilder<T> and(SearchCriteria criteria) {
    if (null != criteria) {
      this.criterias.add(criteria);
    }
    return this;
  }

  /**
   * @param criterias the criterias
   * @return the common predicate builder
   */
  public PredicateBuilder<T> and(List<SearchCriteria> criterias) {
    if (!CollectionUtils.isEmpty(criterias)) {
      this.criterias.addAll(criterias);
    }
    return this;
  }

  /**
   * gets the entity variable.
   *
   * @param simpleClassName the simple class name
   * @return the entity variable
   */
  private static String getEntityVariable(String simpleClassName) {
    char[] c = simpleClassName.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }

  /**
   * builds the predicate.
   *
   * @return the boolean expression
   */
  public BooleanExpression build() {
    BooleanExpression booleanExpression = Expressions.asBoolean(true).isTrue();
    if (!CollectionUtils.isEmpty(criterias)) {
      List<BooleanExpression> predicates =
          criterias.stream()
              .map(
                  c -> {
                    try {
                      return new PredicateBase<>(t, entityVariable)
                          .getPredicate(
                              replaceKeyMap.getOrDefault(c.getKey(), c.getKey()),
                              c.getOperator(),
                              c.getValue());
                    } catch (Exception e) {
                      throw new RuntimeException(e);
                    }
                  })
              .filter(Objects::nonNull)
              .toList();
      for (BooleanExpression predicate : predicates) {
        booleanExpression = booleanExpression.and(predicate);
      }
    }
    return booleanExpression;
  }
}
