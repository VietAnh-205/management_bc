package ck4.nvb.rsmanagement.base.util;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class InputUtils {

  public static String getString(String input) {
    if (input == null) return null;

    String trimmed = input.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public static String getString(Collection<String> input) {
    if (input == null || input.isEmpty()) return null;

    StringBuilder builder = new StringBuilder();
    for (String str : input) if (!str.isEmpty()) builder.append(",").append(str);
    return !builder.isEmpty() ? builder.substring(1) : null;
  }

  public static Long getID(Long id) {
    if (id == null || id < 0) return null;
    return id;
  }

  public static Set<String> toSet(String str) {
    Set<String> result = new TreeSet<>();
    if (str == null) return result;

    String[] values = str.split(",");
    for (String v : values) {
      v = getString(v);
      if (v != null) result.add(v);
    }
    return result;
  }
}
