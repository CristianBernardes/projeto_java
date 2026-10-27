package br.com.projetojava.messaging.errorhandling;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.messaging.errorhandling.chain.BusinessErrorMessageHandler;
import br.com.projetojava.messaging.errorhandling.chain.DeserializationErrorMessageHandler;
import br.com.projetojava.messaging.errorhandling.chain.FallbackMessageErrorHandler;
import br.com.projetojava.messaging.errorhandling.chain.TransientErrorMessageHandler;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

@DisplayName("MessagingErrorHandler (chain assembly)")
class MessagingErrorHandlerTest {

  private static final MessageErrorContext CONTEXT =
      new MessageErrorContext("rabbitmq.order.created.queue", "msg-1");

  private MessagingErrorHandler messagingErrorHandler;

  @BeforeEach
  void setUp() {
    messagingErrorHandler =
        new MessagingErrorHandler(
            new BusinessErrorMessageHandler(),
            new DeserializationErrorMessageHandler(),
            new TransientErrorMessageHandler(),
            new FallbackMessageErrorHandler());
    messagingErrorHandler.buildChain();
  }

  @Test
  @DisplayName(
      "resolve — should route ResourceNotFoundException to REJECT_TO_DEAD_LETTER via BusinessErrorMessageHandler")
  void resolve_shouldRouteBusinessErrorToDeadLetter() {
    MessageErrorResolution resolution =
        messagingErrorHandler.resolve(
            new ResourceNotFoundException("Order not found. id=1"), CONTEXT);

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }

  @Test
  @DisplayName(
      "resolve — should route JsonParseException to REJECT_TO_DEAD_LETTER via DeserializationErrorMessageHandler")
  void resolve_shouldRouteDeserializationErrorToDeadLetter() {
    MessageErrorResolution resolution =
        messagingErrorHandler.resolve(new JsonParseException(null, "bad payload"), CONTEXT);

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }

  @Test
  @DisplayName(
      "resolve — should route DataAccessException to RETRY via TransientErrorMessageHandler")
  void resolve_shouldRouteTransientErrorToRetry() {
    MessageErrorResolution resolution =
        messagingErrorHandler.resolve(new DataAccessResourceFailureException("db down"), CONTEXT);

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.RETRY);
  }

  @Test
  @DisplayName(
      "resolve — should route unknown exceptions to REJECT_TO_DEAD_LETTER via FallbackMessageErrorHandler")
  void resolve_shouldRouteUnknownExceptionToFallback() {
    MessageErrorResolution resolution =
        messagingErrorHandler.resolve(new IllegalStateException("boom"), CONTEXT);

    assertThat(resolution.action()).isEqualTo(MessageErrorAction.REJECT_TO_DEAD_LETTER);
  }
}
