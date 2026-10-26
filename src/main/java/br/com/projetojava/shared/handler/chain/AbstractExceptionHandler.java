package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Base link of the exception handling chain.
 *
 * <p>Implements chaining: each subclass checks whether it can handle the exception via {@link
 * #supports(Exception)}. If so, it delegates to {@link #process(Exception, HttpServletRequest)}.
 * If not, it passes to the next link via {@link #next}.
 *
 * <p>Subclasses only need to implement {@link #supports} and {@link #process}, without worrying
 * about chaining.
 */
public abstract class AbstractExceptionHandler implements ExceptionHandlerChain {

  private ExceptionHandlerChain next;

  /**
   * Sets the next link in the chain.
   *
   * @param next next handler to try if this one does not support the exception
   * @return the next link itself, for fluent chaining
   */
  public AbstractExceptionHandler setNext(AbstractExceptionHandler next) {
    this.next = next;
    return next;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks {@link #supports}: if true, delegates to {@link #process}; otherwise, passes to
   * {@link #next} or returns empty if there is no next.
   */
  @Override
  public final Optional<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
    if (supports(ex)) {
      return Optional.of(process(ex, request));
    }
    if (next != null) {
      return next.handle(ex, request);
    }
    return Optional.empty();
  }

  /**
   * Indicates whether this link is capable of handling the given exception.
   *
   * @param ex exception to evaluate
   * @return {@code true} if this link should process the exception
   */
  protected abstract boolean supports(Exception ex);

  /**
   * Processes the exception and produces the error response.
   *
   * <p>Only called when {@link #supports} returns {@code true}.
   *
   * @param ex exception to handle
   * @param request original HTTP request
   * @return populated error response
   */
  protected abstract ErrorResponse process(Exception ex, HttpServletRequest request);
}
