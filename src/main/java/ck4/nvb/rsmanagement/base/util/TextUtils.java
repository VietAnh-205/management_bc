package ck4.nvb.rsmanagement.base.util;

import java.util.*;
import lombok.Getter;

public class TextUtils {
  private final Map<Character, Character> unicodeMap = new HashMap<>();
  private final Map<Character, Character> toLowerMap = new HashMap<>();
  private final Map<Character, Character> toUpperMap = new HashMap<>();
  private final Set<Character> allowedSymbols =
      new HashSet<>(
          Arrays.asList(
              'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
              'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
              'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
              'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '(', ')', '+', '-', '_', ' ',
              '.', ',', '/', ':', ';'));

  @Getter private static final TextUtils instance = new TextUtils();

  private TextUtils() {
    char[] SOURCE_CHARACTERS = {
      'À', 'Á', 'Â', 'Ã', 'È', 'É', 'Ê', 'Ì', 'Í', 'Ò', 'Ó', 'Ô', 'Õ', 'Ù', 'Ú', 'Ý', 'à', 'á', 'â',
      'ã', 'è', 'é', 'ê', 'ì', 'í', 'ò', 'ó', 'ô', 'õ', 'ù', 'ú', 'ý', 'Ă', 'ă', 'Đ', 'đ', 'Ĩ', 'ĩ',
      'Ũ', 'ũ', 'Ơ', 'ơ', 'Ư', 'ư', 'Ạ', 'ạ', 'Ả', 'ả', 'Ấ', 'ấ', 'Ầ', 'ầ', 'Ẩ', 'ẩ', 'Ẫ', 'ẫ', 'Ậ',
      'ậ', 'Ắ', 'ắ', 'Ằ', 'ằ', 'Ẳ', 'ẳ', 'Ẵ', 'ẵ', 'Ặ', 'ặ', 'Ẹ', 'ẹ', 'Ẻ', 'ẻ', 'Ẽ', 'ẽ', 'Ế', 'ế',
      'Ề', 'ề', 'Ể', 'ể', 'Ễ', 'ễ', 'Ệ', 'ệ', 'Ỉ', 'ỉ', 'Ị', 'ị', 'Ọ', 'ọ', 'Ỏ', 'ỏ', 'Ố', 'ố', 'Ồ',
      'ồ', 'Ổ', 'ổ', 'Ỗ', 'ỗ', 'Ộ', 'ộ', 'Ớ', 'ớ', 'Ờ', 'ờ', 'Ở', 'ở', 'Ỡ', 'ỡ', 'Ợ', 'ợ', 'Ụ', 'ụ',
      'Ủ', 'ủ', 'Ứ', 'ứ', 'Ừ', 'ừ', 'Ử', 'ử', 'Ữ', 'ữ', 'Ự', 'ự', 'Ỳ', 'ỳ', 'Ỵ', 'ỵ', 'Ỷ', 'ỷ', 'Ỹ',
      'ỹ'
    };

    char[] DESTINATION_CHARACTERS = {
      'A', 'A', 'A', 'A', 'E', 'E', 'E', 'I', 'I', 'O', 'O', 'O', 'O', 'U', 'U', 'Y', 'a', 'a', 'a',
      'a', 'e', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'u', 'y', 'A', 'a', 'D', 'd', 'I', 'i',
      'U', 'u', 'O', 'o', 'U', 'u', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A',
      'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e',
      'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'I', 'i', 'I', 'i', 'O', 'o', 'O', 'o', 'O', 'o', 'O',
      'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'U', 'u',
      'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'Y', 'y', 'Y', 'y', 'Y', 'y', 'Y',
      'y'
    };

    char[] LOWER_CHARACTERS = {
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
      't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    char[] UPPER_CHARACTERS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
      'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    for (int i = 0; i < SOURCE_CHARACTERS.length; ++i)
      unicodeMap.put(SOURCE_CHARACTERS[i], DESTINATION_CHARACTERS[i]);

    for (int i = 0; i < UPPER_CHARACTERS.length; ++i)
      toLowerMap.put(UPPER_CHARACTERS[i], LOWER_CHARACTERS[i]);

    for (int i = 0; i < LOWER_CHARACTERS.length; ++i)
      toUpperMap.put(LOWER_CHARACTERS[i], UPPER_CHARACTERS[i]);
  }

  private Character toASCII(char ch, boolean removeDisallowedSymbols, Boolean toLowerCase) {
    char out = ch;
    if (unicodeMap.containsKey(ch)) out = unicodeMap.get(ch);
    if (removeDisallowedSymbols && !allowedSymbols.contains(out)) return null;

    if (toLowerCase == null) return out;
    if (toLowerCase) {
      if (toLowerMap.containsKey(out)) return toLowerMap.get(out);
      return out;
    }
    if (toUpperMap.containsKey(out)) return toUpperMap.get(out);
    return out;
  }

  private String toASCII(String input, boolean removeDisallowedSymbols, Boolean toLowerCase) {
    if (input == null) return null;

    StringBuilder builder = new StringBuilder();
    for (char ch : input.toCharArray()) {
      Character out = toASCII(ch, removeDisallowedSymbols, toLowerCase);
      if (out != null) builder.append(out);
    }
    return builder.toString();
  }

  public Character toASCIIUpperCase(char ch, boolean removeDisallowedSymbols) {
    return toASCII(ch, removeDisallowedSymbols, false);
  }

  public String toASCIIUpperCase(String input, boolean removeDisallowedSymbols) {
    return toASCII(input, removeDisallowedSymbols, false);
  }

  public Character toASCIILowerCase(char ch, boolean removeDisallowedSymbols) {
    return toASCII(ch, removeDisallowedSymbols, true);
  }

  public String toASCIILowerCase(String input, boolean removeDisallowedSymbols) {
    return toASCII(input, removeDisallowedSymbols, true);
  }

  public String toTrimmedText(String input) {
    if (input == null) return null;
    String trimmed = input.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public String toSearchText(String input) {
    return toASCIIUpperCase(input, true).replaceAll(" ", "");
  }
}
