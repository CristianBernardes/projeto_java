package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 405 Method Not Allowed} — the HTTP method used is not supported by the endpoint.
 *
 * <p>Normally thrown by Spring MVC itself, but can be used explicitly when the method restriction
 * is checked at the application layer.
 */
public class MethodNotAllowedException extends ApiException {

  /**
   * @param message error description
   */
  public MethodNotAllowedException(String message) {
    super(HttpStatus.METHOD_NOT_ALLOWED, message);
  }

  /**
   * @param message error description
   * @param cause original exception
   */
  public MethodNotAllowedException(String message, Throwable cause) {
    super(HttpStatus.METHOD_NOT_ALLOWED, message, cause);
  }
}
