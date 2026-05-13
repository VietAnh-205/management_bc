package ck4.nvb.rsmanagement.base.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
  String[] value(); // Thay đổi từ String thành String[]

  // định nghĩa logic: ALL (AND) hoặc ANY (OR)
  LogicType logic() default LogicType.ALL;

  enum LogicType {
    ALL, // User phải có TẤT CẢ permissions (AND logic)
    ANY // User chỉ cần có ÍT NHẤT MỘT permission (OR logic)
  }
}
