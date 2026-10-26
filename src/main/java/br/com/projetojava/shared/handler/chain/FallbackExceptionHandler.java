package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Link 4 (last) — fallback for any exception not handled by previous links.
 *
 * <p>Always returns {@code 500 Internal Server Error} with a generic message, without exposing
 * internal details to the client. The full stack trace is logged for diagnostics.
 *
 * <p>This link never passes to a next: it is the mandatory end of the chain.
 */
@Slf4j
@Component
public class FallbackExceptionHandler extends AbstractExceptionHandler {

  /**
   * {@inheritDoc}
   *
   * @return always {@code true} — this link handles any exception
   */
  @Override
  protected boolean supports(Exception ex) {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Logs the full error internally and returns a generic {@code 500} response to the client,
   * without exposing technical details.
   */
  @Override
  protected ErrorResponse process(Exception ex, HttpServletRequest request) {
    log.error(
        "Unexpected error at {} {}: {}",
        request.getMethod(),
        request.getRequestURI(),
        ex.getMessage(),
        ex);

    return ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An internal error occurred. Please try again later.",
        request.getRequestURI());
  }
}
