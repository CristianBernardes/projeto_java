package br.com.projetojava.shared.exception.http5xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 503 Service Unavailable} — the server is temporarily unavailable.
 *
 * <p>Use when an external dependency (database, third-party service, queue) is down or overloaded,
 * making it impossible to serve the request at this time.
 */
public class ServiceUnavailableException extends ApiException {

  /**
   * @param message description of the unavailability
   */
  public ServiceUnavailableException(String message) {
    super(HttpStatus.SERVICE_UNAVAILABLE, message);
  }

  /**
   * @param message description of the unavailability
   * @param cause original exception
   */
  public ServiceUnavailableException(String message, Throwable cause) {
    super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
  }
}
