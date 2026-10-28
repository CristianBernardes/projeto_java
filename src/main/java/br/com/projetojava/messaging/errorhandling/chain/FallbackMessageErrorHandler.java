package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Link 4 (last) — fallback for any exception not handled by previous links.
 *
 * <p>Mirrors {@code br.com.projetojava.shared.handler.chain.FallbackExceptionHandler}: an
 * <strong>unknown</strong> exception is treated conservatively as permanent rather than transient.
 * Defaulting to {@code RETRY} for an unrecognized error risks an infinite redelivery loop (poison
 * message blocking the whole queue); defaulting to dead-letter is safer — the message is preserved
 * for inspection instead of being silently lost or looping forever.
 *
 * <p>This link never passes to a next: it is the mandatory end of the chain.
 */
@Slf4j
@Component
public class FallbackMessageErrorHandler extends AbstractMessageErrorHandler {

  /**
   * {@inheritDoc}
   *
   * @return always {@code true} — this link handles any exception
   */
  @Override
  protected boolean supports(Exception ex) {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Logs the full error internally and resolves to {@link
   * br.com.projetojava.messaging.errorhandling.MessageErrorAction#REJECT_TO_DEAD_LETTER}.
   */
  @Override
  protected MessageErrorResolution process(Exception ex, MessageErrorContext context) {
    log.error(
        "Erro inesperado para message_id='{}' de source='{}'. Enviando para dead letter para"
            + " inspeção manual.",
        context.messageId(),
        context.source(),
        ex);
    return MessageErrorResolution.rejectToDeadLetter(
        "Ocorreu um erro inesperado ao processar a mensagem.");
  }
}
