package br.com.projetojava.messaging.errorhandling.chain;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.messaging.errorhandling.MessageErrorAction;
import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;

@DisplayName("TransientErrorMessageHandler")
class TransientErrorMessageHandlerTest {

  private final TransientErrorMessageHandler handler = new TransientErrorMessageHandler();
  private static final MessageErrorContext CONTEXT =
      new MessageErrorContext("rabbitmq.order.created.queue", "msg-1");

  @Test
  @DisplayName("handle — should resolve DataAccessException to RETRY")
  void handle_shouldRetryForDataAccessException() {
    DataAccessResourceFailureException ex = new DataAccessResourceFailureException("db down");

    MessageErrorResolution resolution = handler.handle(ex, CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.RETRY);
  }

  @Test
  @DisplayName("handle — should resolve OptimisticLockingFailureException to RETRY")
  void handle_shouldRetryForOptimisticLockingFailureException() {
    OptimisticLockingFailureException ex =
        new OptimisticLockingFailureException("version conflict");

    MessageErrorResolution resolution = handler.handle(ex, CONTEXT).orElseThrow();

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.RETRY);
  }

  @Test
  @DisplayName("handle — should pass through (empty) for exceptions it does not support")
  void handle_shouldPassThroughForUnsupportedException() {
    assertThat(handler.handle(new RuntimeException("generic"), CONTEXT)).isEmpty();
  }
}
