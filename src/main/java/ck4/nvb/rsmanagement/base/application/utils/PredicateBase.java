package ck4.nvb.rsmanagement.base.application.utils;

import ck4.nvb.rsmanagement.base.application.exception.InvalidFormatException;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import com.querydsl.core.types.dsl.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Su dung QueryDSL
/**
 * @param <T> generic type
 */
public class PredicateBase<T> {

  private final Class<? extends T> type;
  private final String entityVariable;

  /**
   * initiates a new common predicate
   *
   * @param type the type
   * @param entityVariable the entity variable
   */
  public PredicateBase(Class<? extends T> type, String entityVariable) {
    this.type = type;
    this.entityVariable = entityVariable;
  }

  /**
   * get the predicate
   *
   * @param key key
   * @param operator operator
   * @param value value
   * @return predicate
   */
  public BooleanExpression getPredicate(String key, SearchOperator operator, String value)
      throws Exception {
    PathBuilder<T> entityPath = new PathBuilder<>(type, entityVariable);
    return getPredicate(key, operator, value, entityPath, type);
  }

  /**
   * @param key the key
   * @param operator the operator
   * @param value the value
   * @param entityPath the entity path
   * @param classType the class type
   * @return Predicate
   */
  private BooleanExpression getPredicate(
      String key,
      SearchOperator operator,
      String value,
      PathBuilder<?> entityPath,
      Class<?> classType)
      throws Exception {
    final String multivalueSeperator = "_";
    boolean isMultiValue = value.contains(multivalueSeperator);
    Class<?> propertyType = getPropertyType(classType, key);
    switch (propertyType.getSimpleName()) {
      case "Integer":
        {
          NumberPath<Integer> path = entityPath.getNumber(key, Integer.class);
          if (isMultiValue) {
            return getNumberPredicate(
                path,
                operator,
                Stream.of(value.split(multivalueSeperator))
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new));
          } else {
            return getNumberPredicate(path, operator, Integer.parseInt(value));
          }
        }
      case "Long":
        {
          NumberPath<Long> path = entityPath.getNumber(key, Long.class);
          if (isMultiValue) {
            return getNumberPredicate(
                path,
                operator,
                Stream.of(value.split(multivalueSeperator))
                    .map(Long::parseLong)
                    .toArray(Long[]::new));
          } else {
            return getNumberPredicate(path, operator, Long.parseLong(value));
          }
        }
      case "Double":
        {
          NumberPath<Double> path = entityPath.getNumber(key, Double.class);
          if (isMultiValue) {
            return getNumberPredicate(
                path,
                operator,
                Stream.of(value.split(multivalueSeperator))
                    .map(Double::parseDouble)
                    .toArray(Double[]::new));
          } else {
            return getNumberPredicate(path, operator, Double.parseDouble(value));
          }
        }
      case "Boolean":
      case "boolean":
        {
          if (operator == SearchOperator.EQUALS) {
            return entityPath.getBoolean(key).eq(Boolean.parseBoolean(value));
          } else {
            throw new InvalidFormatException("Unsupported boolean operation in filter query.");
          }
        }

      case "String":
        {
          return getStringPredicate(key, operator, value, entityPath);
        }
      case "LocalDate":
        {
          return getDatePredicate(key, operator, value, entityPath);
        }
      case "LocalDateTime":
        {
          return getDateTimePredicate(key, operator, value, entityPath);
        }
      default:
        {
          // do nothing hehe
          return null;
        }
    }
  }

  /**
   * gets the number predicate
   *
   * @param path
   * @param operator
   * @param value
   * @return
   */
  private <N extends Number & Comparable<?>> BooleanExpression getNumberPredicate(
      NumberPath<N> path, SearchOperator operator, N value) {
    return switch (operator) {
      case EQUALS -> path.eq(value);
      case NOT_IN -> path.notIn(value);
      case GREATER_THAN -> path.gt(value);
      case LESS_THAN -> path.lt(value);
      case GREATER_THAN_OR_EQUAL -> path.goe(value);
      case LESS_THAN_OR_EQUAL -> path.loe(value);
      default -> null;
    };
  }

  /**
   * get the number predicate
   *
   * @param path
   * @param operator
   * @param numValues
   * @return
   */
  private BooleanExpression getNumberPredicate(
      NumberPath<?> path, SearchOperator operator, Object[] numValues) {
    Number[] valuesArray = (Number[]) numValues;
    return switch (operator) {
      case EQUALS -> path.in(valuesArray);
      case BETWEEN -> path.between(valuesArray[0].doubleValue(), valuesArray[1].doubleValue());
      case NOT_IN -> path.notIn(valuesArray);
      default -> null;
    };
  }

  /**
   * get date predicate
   *
   * @param key the key
   * @param operator the operator
   * @param value the value
   * @param entityPath the entity path
   * @return date predicate
   */
  private BooleanExpression getDatePredicate(
      String key, SearchOperator operator, String value, PathBuilder<?> entityPath) {
    DatePath<LocalDate> path = entityPath.getDate(key, LocalDate.class);

    if (value.contains(",")) {
      List<LocalDate> dateValues =
          Stream.of(value.split(",")).map(LocalDate::parse).collect(Collectors.toList());
      return switch (operator) {
        case EQUALS -> path.in(dateValues);
        case BETWEEN -> path.between(dateValues.get(0), dateValues.get(1));
        default -> null;
      };
    } else {
      LocalDate dateValue = LocalDate.parse(value);
      return switch (operator) {
        case EQUALS -> path.eq(dateValue);
        case GREATER_THAN -> path.gt(dateValue);
        case LESS_THAN -> path.lt(dateValue);
        case GREATER_THAN_OR_EQUAL -> path.goe(dateValue);
        case LESS_THAN_OR_EQUAL -> path.loe(dateValue);
        default -> null;
      };
    }
  }

  /**
   * @param key the key
   * @param operator thr operator
   * @param value the value
   * @param entityPath entity Path
   * @return the date time predicate
   */
  private BooleanExpression getDateTimePredicate(
      String key, SearchOperator operator, String value, PathBuilder<?> entityPath)
      throws Exception {
    DatePath<LocalDateTime> path = entityPath.getDate(key, LocalDateTime.class);
    if (value.contains(",")) {
      List<LocalDate> dateTimeValues =
          Stream.of(value.split(",")).map(LocalDate::parse).collect(Collectors.toList());
      return switch (operator) {
        case EQUALS ->
            path.in(
                dateTimeValues.parallelStream()
                    .map(LocalDate::atStartOfDay)
                    .collect(Collectors.toList()));
        case BETWEEN ->
            path.between(
                dateTimeValues.get(0).atStartOfDay(), dateTimeValues.get(1).atTime(LocalTime.MAX));
        default -> null;
      };
    } else {
      LocalDate dateValue = LocalDate.parse(value);
      return switch (operator) {
        case EQUALS -> path.between(dateValue.atStartOfDay(), dateValue.atTime(LocalTime.MAX));
        case GREATER_THAN -> path.gt(dateValue.atTime(LocalTime.MAX));
        case LESS_THAN -> path.lt(dateValue.atStartOfDay());
        case GREATER_THAN_OR_EQUAL -> path.goe(dateValue.atStartOfDay());
        case LESS_THAN_OR_EQUAL -> path.loe(dateValue.atTime(LocalTime.MAX));
        default -> null;
      };
    }
  }

  /**
   * Get the string predicate
   *
   * @param key the key
   * @param operator the operator
   * @param value the value
   * @param entityPath the entity path
   * @return the string predicate
   */
  private BooleanExpression getStringPredicate(
      String key, SearchOperator operator, String value, PathBuilder<?> entityPath)
      throws Exception {
    StringPath path = entityPath.getString(key);
    if (value.contains(",")) {
      return path.in(Stream.of(value.split(",")).collect(Collectors.toList()));
    }

    return switch (operator) {
      case EQUALS -> path.equalsIgnoreCase(value);
      case CONTAINS -> path.containsIgnoreCase(value);
      default -> null;
    };
  }

  /**
   * Get the property type
   *
   * @param parent the parent
   * @param property the property
   * @return the property type
   */
  private Class<?> getPropertyType(Class<?> parent, String property) throws Exception {
    List<String> propertiesList = new LinkedList<>(Arrays.asList(property.split("\\.")));
    return getRecursiveType(parent, propertiesList);
  }

  /**
   * Get the property type.
   *
   * @param parent the parent
   * @param propertiesList the property list
   * @return the recursive type
   */
  private Class<?> getRecursiveType(Class<?> parent, List<String> propertiesList) throws Exception {
    if (propertiesList.size() > 1) {
      Field field = parent.getDeclaredField(propertiesList.get(0));
      Class<?> child = field.getType();
      propertiesList.remove(propertiesList.get(0));
      if ("List".equals(child.getSimpleName())) {
        return child;
      }
      return getRecursiveType(child, propertiesList);
    } else {
      return getInheritType(parent, propertiesList.get(0));
    }
  }

  private Class<?> getInheritType(Class<?> clazz, String property) {
    Class<?> currentClass = clazz;

    // traverse
    while (currentClass != null) {
      try {
        Field field = currentClass.getDeclaredField(property);
        return field.getType(); // return field's data type
      } catch (NoSuchFieldException e) {
        // if field not found in current class, continue with superclass
        currentClass = currentClass.getSuperclass();
      }
    }

    // if field is not found in hierarchy, return null
    return null;
  }
}
