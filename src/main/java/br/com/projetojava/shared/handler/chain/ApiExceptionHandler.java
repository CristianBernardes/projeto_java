package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.exception.ApiException;
import br.com.projetojava.shared.exception.http4xx.UnprocessableEntityException;
import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Link 1 — handles any subclass of {@link ApiException}.
 *
 * <p>Since all domain exceptions in the application extend {@link ApiException}, this link covers
 * all intentional errors thrown by business code, extracting the {@code HttpStatus} directly from
 * the exception.
 *
 * <p>If the exception is an {@link UnprocessableEntityException}, it includes the field error list
 * in the response.
 */
@Component
public class ApiExceptionHandler extends AbstractExceptionHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for any subclass of {@link ApiException}
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof ApiException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Extracts the HTTP status from the exception itself. If it is an {@link
   * UnprocessableEntityException}, includes the detailed error list.
   */
  @Override
  protected ErrorResponse process(Exception ex, HttpServletRequest request) {
    ApiException apiEx = (ApiException) ex;

    if (apiEx instanceof UnprocessableEntityException unprocessable) {
      return ErrorResponse.withErrors(
          apiEx.getStatus(),
          apiEx.getMessage(),
          unprocessable.getErrors(),
          request.getRequestURI());
    }

    return ErrorResponse.of(apiEx.getStatus(), apiEx.getMessage(), request.getRequestURI());
  }
}
