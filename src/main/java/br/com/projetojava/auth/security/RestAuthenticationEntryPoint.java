package br.com.projetojava.auth.security;

import br.com.projetojava.shared.handler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Handles requests to protected endpoints with no (or invalid) authentication.
 *
 * <p>Spring Security intercepts unauthenticated requests <em>before</em> they reach any controller,
 * so {@code GlobalExceptionHandler} never sees them. Without this entry point, Spring Security's
 * default behavior returns a bare {@code 403 Forbidden} with no body — inconsistent with every
 * other error in the API. This class reuses {@link ErrorResponse} so a missing/invalid JWT produces
 * the exact same {@code 401} contract as every other error.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  /**
   * Writes a standardized {@code 401 Unauthorized} {@link ErrorResponse} body.
   *
   * @param request original HTTP request
   * @param response HTTP response to write to
   * @param authException exception describing why authentication failed
   * @throws IOException if writing the response body fails
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.UNAUTHORIZED,
            "Authentication is required to access this resource.",
            request.getRequestURI());

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
