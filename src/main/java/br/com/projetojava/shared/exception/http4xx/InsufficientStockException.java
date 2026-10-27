package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 409 Conflict} — the requested operation cannot be completed because of insufficient stock
 * for one or more products.
 *
 * <p>Thrown by the asynchronous stock reservation consumer when an order's requested quantity
 * exceeds the available stock at the moment of processing. Triggers a compensating action (the
 * order is marked as {@code FAILED} and a compensation event is published) instead of a synchronous
 * HTTP response, since stock reservation happens after the order is accepted.
 */
public class InsufficientStockException extends ApiException {

  /**
   * @param message description of the insufficient stock situation
   */
  public InsufficientStockException(String message) {
    super(HttpStatus.CONFLICT, message);
  }

  /**
   * @param message description of the insufficient stock situation
   * @param cause original exception
   */
  public InsufficientStockException(String message, Throwable cause) {
    super(HttpStatus.CONFLICT, message, cause);
  }
}
