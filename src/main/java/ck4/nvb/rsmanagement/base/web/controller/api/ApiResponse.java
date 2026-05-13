package ck4.nvb.rsmanagement.base.web.controller.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().code(200).message("Success").data(data).build();
  }

  public static <T> ApiResponse<T> error(int code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).build();
  }
}
