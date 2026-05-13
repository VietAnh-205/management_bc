package ck4.nvb.rsmanagement.core.web.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordEncoder {
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private static final int DEFAULT_STRENGTH = 12; // Độ mạnh mặc định

  /**
   * Mã hóa password thành BCrypt hash với độ mạnh mặc định
   *
   * @param plainPassword password gốc cần mã hóa
   * @return BCrypt hash của password
   * @throws IllegalArgumentException nếu password null hoặc rỗng
   */
  public static String encodePassword(String plainPassword) {
    if (plainPassword == null || plainPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Password không được null hoặc rỗng");
    }
    return encoder.encode(plainPassword);
  }

  /**
   * Mã hóa password với độ mạnh tùy chỉnh
   *
   * @param plainPassword password gốc cần mã hóa
   * @param strength độ mạnh từ 4-31 (càng cao càng an toàn nhưng chậm hơn)
   * @return BCrypt hash của password
   * @throws IllegalArgumentException nếu password null/rỗng hoặc strength không hợp lệ
   */
  public static String encodePassword(String plainPassword, int strength) {
    if (plainPassword == null || plainPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Password không được null hoặc rỗng");
    }
    if (strength < 4 || strength > 31) {
      throw new IllegalArgumentException("Strength phải từ 4 đến 31");
    }

    BCryptPasswordEncoder customEncoder = new BCryptPasswordEncoder(strength);
    return customEncoder.encode(plainPassword);
  }

  /**
   * Kiểm tra password có khớp với hash đã lưu không
   *
   * @param plainPassword password gốc
   * @param hashedPassword BCrypt hash đã lưu
   * @return true nếu password khớp, false nếu không
   */
  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    if (plainPassword == null || hashedPassword == null) {
      return false;
    }
    return encoder.matches(plainPassword, hashedPassword);
  }

  /**
   * Kiểm tra xem một chuỗi có phải là BCrypt hash hợp lệ không
   *
   * @param hash chuỗi cần kiểm tra
   * @return true nếu là BCrypt hash hợp lệ
   */
  public static boolean isBCryptHash(String hash) {
    if (hash == null || hash.length() != 60) {
      return false;
    }
    return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
  }

  public static void main(String[] args) {
    // Ví dụ 1: Mã hóa password đơn giản
    String password = "admin1234";
    String hashedPassword = encodePassword(password);
    System.out.println("Password gốc: " + password);
    System.out.println("BCrypt hash: " + hashedPassword);
  }
}
