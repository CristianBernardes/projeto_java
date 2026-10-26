package br.com.projetojava.shared.exception.http5xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 501 Not Implemented} — the server does not support the functionality required to fulfill
 * the request.
 *
 * <p>Use for planned but not yet implemented endpoints, signaling to the client that the feature is
 * planned but unavailable.
 */
public class NotImplementedException extends ApiException {

  /**
   * @param message description of the unimplemented feature
   */
  public NotImplementedException(String message) {
    super(HttpStatus.NOT_IMPLEMENTED, message);
  }

  /**
   * @param message description of the unimplemented feature
   * @param cause original exception
   */
  public NotImplementedException(String message, Throwable cause) {
    super(HttpStatus.NOT_IMPLEMENTED, message, cause);
  }
}
