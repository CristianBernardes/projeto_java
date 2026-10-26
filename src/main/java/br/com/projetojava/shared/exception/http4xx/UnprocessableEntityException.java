package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * {@code 422 Unprocessable Entity} — the request is well-formed but contains semantic errors.
 *
 * <p>Use when data passes syntax validation ({@code 400}) but violates business rules or field
 * validation (Bean Validation). Can carry a list of detailed errors.
 */
public class UnprocessableEntityException extends ApiException {

  private final List<String> errors;

  /**
   * @param message main message
   * @param errors list of detailed field errors
   */
  public UnprocessableEntityException(String message, List<String> errors) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    this.errors = errors != null ? List.copyOf(errors) : List.of();
  }

  /**
   * @param message main message without error list
   */
  public UnprocessableEntityException(String message) {
    this(message, List.of());
  }

  /**
   * Returns the immutable list of field errors.
   *
   * @return list of detailed errors
   */
  public List<String> getErrors() {
    return errors;
  }
}
