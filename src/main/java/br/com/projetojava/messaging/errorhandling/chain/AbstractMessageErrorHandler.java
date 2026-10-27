package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import java.util.Optional;

/**
 * Base link of the message error handling chain.
 *
 * <p>Mirrors {@code br.com.projetojava.shared.handler.chain.AbstractExceptionHandler}: implements
 * chaining so each subclass only needs {@link #supports(Exception)} and {@link #process(Exception,
 * MessageErrorContext)}, without worrying about delegating to the next link.
 */
public abstract class AbstractMessageErrorHandler implements MessageErrorHandlerChain {

  private MessageErrorHandlerChain next;

  /**
   * Sets the next link in the chain.
   *
   * @param next next handler to try if this one does not support the exception
   * @return the next link itself, for fluent chaining
   */
  public AbstractMessageErrorHandler setNext(AbstractMessageErrorHandler next) {
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
  public final Optional<MessageErrorResolution> handle(Exception ex, MessageErrorContext context) {
    if (supports(ex)) {
      return Optional.of(process(ex, context));
    }
    if (next != null) {
      return next.handle(ex, context);
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
   * Processes the exception and produces the resolution.
   *
   * <p>Only called when {@link #supports} returns {@code true}.
   *
   * @param ex exception to handle
   * @param context metadata about the message being processed
   * @return populated resolution
   */
  protected abstract MessageErrorResolution process(Exception ex, MessageErrorContext context);
}
