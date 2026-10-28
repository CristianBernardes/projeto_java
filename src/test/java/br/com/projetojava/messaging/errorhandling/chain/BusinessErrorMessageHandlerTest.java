package br.com.projetojava.messaging.errorhandling.chain;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.messaging.errorhandling.MessageErrorAction;
import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BusinessErrorMessageHandler")
class BusinessErrorMessageHandlerTest {

  private final BusinessErrorMessageHandler handler = new BusinessErrorMessageHandler();
  private static final MessageErrorContext CONTEXT =
      new MessageErrorContext("rabbitmq.order.created.queue", "msg-1");

  @Test
  @DisplayName("handle — should resolve ResourceNotFoundException to REJECT_TO_DEAD_LETTER")
  void handle_shouldRejectToDeadLetterForResourceNotFound() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Pedido não encontrado. id=1");

    MessageErrorResolution resolution = handler.handle(ex, CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
    assertThat(resolution.reason()).contains("Pedido não encontrado");
  }

  @Test
  @DisplayName("handle — should pass through (empty) for exceptions it does not support")
  void handle_shouldPassThroughForUnsupportedException() {
    assertThat(handler.handle(new RuntimeException("generic"), CONTEXT)).isEmpty();
  }
}
