package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.stereotype.Component;

/**
 * Link 2 — handles malformed/unparseable message payloads.
 *
 * <p>A message that cannot be deserialized into the expected event type is a classic <strong>poison
 * message</strong>: it will fail identically on every retry attempt (the bytes on the queue never
 * change), so retrying only wastes attempts and delays healthy messages behind it. This link sends
 * it straight to the dead-letter queue for manual inspection.
 */
@Slf4j
@Component
public class DeserializationErrorMessageHandler extends AbstractMessageErrorHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for JSON parsing or AMQP message conversion failures
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof JsonProcessingException || ex instanceof MessageConversionException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Always resolves to {@link
   * br.com.projetojava.messaging.errorhandling.MessageErrorAction#REJECT_TO_DEAD_LETTER}.
   */
  @Override
  protected MessageErrorResolution process(Exception ex, MessageErrorContext context) {
    log.error(
        "Poison message (unparseable payload) for message_id='{}' from source='{}'. Routing to"
            + " dead letter without retry.",
        context.messageId(),
        context.source(),
        ex);
    return MessageErrorResolution.rejectToDeadLetter(
        "Malformed message payload: " + ex.getMessage());
  }
}
