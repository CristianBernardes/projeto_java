package br.com.projetojava.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter executed once per request.
 *
 * <p>Extracts the bearer token from the {@code Authorization} header, validates it via {@link
 * JwtService}, and sets the {@link org.springframework.security.core.context.SecurityContext} so
 * that downstream filters and controllers see an authenticated principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  /**
   * Processes the JWT token from the request header and populates the security context.
   *
   * @param request incoming HTTP request
   * @param response HTTP response
   * @param filterChain remaining filter chain
   * @throws ServletException if a servlet error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    if (!jwtService.isTokenValid(token)) {
      log.warn("Invalid JWT token for request: {}", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

    String email = jwtService.extractSubject(token);
    String role = jwtService.extractRole(token);

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }
}
