package ck4.nvb.rsmanagement.base.web.utils;

import ck4.nvb.rsmanagement.base.application.exception.InvalidFormatException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCriteriaParser {

  private static final Pattern pattern =
      Pattern.compile(
          "([\\p{L}0-9_.-]+?)(:|<>|<|>|<:|>:|~|\\(\\))(.+?),",
          Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL);

  public static List<SearchCriteria> parse(List<String> filter) {
    List<SearchCriteria> criterias = new ArrayList<>();
    if (filter != null) {
      // decode and validate each filter string
      List<SearchCriteria> collect =
          filter.parallelStream().map(SearchCriteriaParser::validateFilterPattern).toList();
      criterias.addAll(collect);
    }
    return criterias;
  }

  private static SearchCriteria validateFilterPattern(String rawFilter) {
    // decode URL encoding (in case request passed encoded)
    String filter = URLDecoder.decode(rawFilter == null ? "" : rawFilter, StandardCharsets.UTF_8);

    // ensure trailing comma so regex can pick value until comma
    Matcher matcher = pattern.matcher(filter + ",");
    if (matcher.find()) {
      SearchOperator operator = SearchOperator.getOperator(matcher.group(2));
      if (operator == null) {
        throw new InvalidFormatException("Invalid operator format: " + matcher.group(2));
      }
      String key = matcher.group(1);
      String value = matcher.group(3).trim();
      return new SearchCriteria(key, operator, value);
    } else {
      throw new InvalidFormatException("Invalid filter format: " + rawFilter);
    }
  }
}
