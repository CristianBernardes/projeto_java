package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Contract for a link in the exception handling chain (Chain of Responsibility).
 *
 * <p>Each link decides whether it can handle the received exception. If so, it returns a populated
 * {@link ErrorResponse}. If not, it returns {@link Optional#empty()} so the next link can try.
 *
 * <p>The chain is assembled by {@link br.com.projetojava.shared.handler.GlobalExceptionHandler} and
 * traversed in order until a link produces a response or the fallback link ends with {@code 500}.
 */
public interface ExceptionHandlerChain {

  /**
   * Attempts to handle the given exception.
   *
   * @param ex exception to handle
   * @param request original HTTP request (to extract path, method, etc.)
   * @return {@link Optional} with the error response if this link handled it, or empty to pass to
   *     the next
   */
  Optional<ErrorResponse> handle(Exception ex, HttpServletRequest request);
}
