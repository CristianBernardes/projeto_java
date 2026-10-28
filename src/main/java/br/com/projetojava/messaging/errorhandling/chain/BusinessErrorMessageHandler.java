package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Link 1 — handles permanent business errors that retrying will never fix.
 *
 * <p>{@link ResourceNotFoundException} means the order or product referenced by the message no
 * longer exists (or never existed) — retrying will keep failing identically, so the message is
 * routed straight to the dead-letter queue instead of wasting retry attempts and delaying other
 * messages behind it in the queue.
 */
@Slf4j
@Component
public class BusinessErrorMessageHandler extends AbstractMessageErrorHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for {@link ResourceNotFoundException}
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof ResourceNotFoundException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Always resolves to {@link
   * br.com.projetojava.messaging.errorhandling.MessageErrorAction#REJECT_TO_DEAD_LETTER}.
   */
  @Override
  protected MessageErrorResolution process(Exception ex, MessageErrorContext context) {
    log.warn(
        "Erro de negócio permanente para message_id='{}' de source='{}': {}. Enviando para dead"
            + " letter.",
        context.messageId(),
        context.source(),
        ex.getMessage());
    return MessageErrorResolution.rejectToDeadLetter(ex.getMessage());
  }
}
