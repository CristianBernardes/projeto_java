package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Link 2 — handles Bean Validation errors ({@link MethodArgumentNotValidException}).
 *
 * <p>Captures {@code @Valid} failures in controllers and transforms the field error list into a
 * {@code 422 Unprocessable Entity} response with per-field details.
 */
@Component
public class ValidationExceptionHandler extends AbstractExceptionHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for {@link MethodArgumentNotValidException}
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof MethodArgumentNotValidException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Extracts field errors from the {@code BindingResult} and includes them in the response.
   */
  @Override
  protected ErrorResponse process(Exception ex, HttpServletRequest request) {
    MethodArgumentNotValidException validEx = (MethodArgumentNotValidException) ex;

    List<String> errors =
        validEx.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();

    return ErrorResponse.withErrors(
        HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos.", errors, request.getRequestURI());
  }
}
