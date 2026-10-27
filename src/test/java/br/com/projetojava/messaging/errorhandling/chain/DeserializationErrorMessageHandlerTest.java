package br.com.projetojava.messaging.errorhandling.chain;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.messaging.errorhandling.MessageErrorAction;
import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.MessageConversionException;

@DisplayName("DeserializationErrorMessageHandler")
class DeserializationErrorMessageHandlerTest {

  private final DeserializationErrorMessageHandler handler =
      new DeserializationErrorMessageHandler();
  private static final MessageErrorContext CONTEXT =
      new MessageErrorContext("rabbitmq.order.created.queue", "msg-1");

  @Test
  @DisplayName("handle — should resolve JsonProcessingException to REJECT_TO_DEAD_LETTER")
  void handle_shouldRejectToDeadLetterForJsonProcessingException() {
    JsonParseException ex = new JsonParseException(null, "unexpected token");

    MessageErrorResolution resolution = handler.handle(ex, CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }

  @Test
  @DisplayName("handle — should resolve MessageConversionException to REJECT_TO_DEAD_LETTER")
  void handle_shouldRejectToDeadLetterForMessageConversionException() {
    MessageConversionException ex = new MessageConversionException("cannot convert");

    MessageErrorResolution resolution = handler.handle(ex, CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }

  @Test
  @DisplayName("handle — should pass through (empty) for exceptions it does not support")
  void handle_shouldPassThroughForUnsupportedException() {
    assertThat(handler.handle(new RuntimeException("generic"), CONTEXT)).isEmpty();
  }
}
