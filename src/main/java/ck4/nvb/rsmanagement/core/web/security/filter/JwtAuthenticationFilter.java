package ck4.nvb.rsmanagement.core.web.security.filter;

import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Value("${jwt.secret}")
  private String JWT_SECRET;

  @Autowired private UserRepository userRepository;

  private Long toLong(Object value) {
    if (value == null) return null;
    if (value instanceof Number n) return n.longValue();
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = extractToken(request);
    if (token != null && validateToken(token)) {
      Claims claims = getClaims(token);
      String username = claims.getSubject();

      Long userId = toLong(claims.get("userId"));
      Long storeId = toLong(claims.get("storeId"));

      List<String> roles = toStringList(claims.get("roles"));
      List<String> permissions = toStringList(claims.get("permissions"));

      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      authorities.addAll(
          roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
      authorities.addAll(
          permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

      UserGetDto userDto = new UserGetDto();
      userDto.setId(userId);
      userDto.setUserName(username);
      userDto.setStoreId(storeId);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userDto, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    filterChain.doFilter(request, response);
  }

  // parse claims with signing key
  private Claims getClaims(String token) {
    byte[] keyBytes = Base64.getDecoder().decode(JWT_SECRET);
    SecretKey key = Keys.hmacShaKeyFor(keyBytes);

    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  private boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (Exception e) {
      log.debug("Invalid JWT: {}", e.getMessage());
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> toStringList(Object obj) {
    if (obj == null) return new ArrayList<>();
    if (obj instanceof List) {
      return ((List<?>) obj).stream().map(Object::toString).collect(Collectors.toList());
    }
    return List.of(obj.toString());
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
