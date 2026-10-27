package br.com.projetojava.auth.security;

import br.com.projetojava.shared.handler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Handles requests from an authenticated user who lacks permission for the resource.
 *
 * <p>Complements {@link RestAuthenticationEntryPoint}: that class handles "who are you?" ({@code
 * 401}), this one handles "you are known, but not allowed" ({@code 403}). Both reuse {@link
 * ErrorResponse} so every Spring Security rejection matches the same error contract as exceptions
 * thrown inside controllers.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  /**
   * Writes a standardized {@code 403 Forbidden} {@link ErrorResponse} body.
   *
   * @param request original HTTP request
   * @param response HTTP response to write to
   * @param accessDeniedException exception describing why access was denied
   * @throws IOException if writing the response body fails
   */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource.",
            request.getRequestURI());

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
