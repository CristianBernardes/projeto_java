package br.com.projetojava.messaging.errorhandling.chain;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.messaging.errorhandling.MessageErrorAction;
import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FallbackMessageErrorHandler")
class FallbackMessageErrorHandlerTest {

  private final FallbackMessageErrorHandler handler = new FallbackMessageErrorHandler();
  private static final MessageErrorContext CONTEXT =
      new MessageErrorContext("rabbitmq.order.created.queue", "msg-1");

  @Test
  @DisplayName("handle — should resolve any exception to REJECT_TO_DEAD_LETTER")
  void handle_shouldRejectToDeadLetterForAnyException() {
    MessageErrorResolution resolution =
        handler.handle(new IllegalStateException("boom"), CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }
}
