package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 401 Unauthorized} — authentication required or invalid credentials.
 *
 * <p>Despite the HTTP name, semantically means "not authenticated": the client must identify itself
 * to obtain the requested response.
 */
public class UnauthorizedException extends ApiException {

  /**
   * @param message error description
   */
  public UnauthorizedException(String message) {
    super(HttpStatus.UNAUTHORIZED, message);
  }

  /**
   * @param message error description
   * @param cause original exception
   */
  public UnauthorizedException(String message, Throwable cause) {
    super(HttpStatus.UNAUTHORIZED, message, cause);
  }
}
