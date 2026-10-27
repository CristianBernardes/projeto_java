package br.com.projetojava.order.messaging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import br.com.projetojava.messaging.errorhandling.MessagingErrorHandlerPort;
import br.com.projetojava.messaging.idempotency.IdempotencyGuardPort;
import br.com.projetojava.order.event.OrderCreatedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReservationConsumer")
class StockReservationConsumerTest {

  @Mock private StockReservationServicePort stockReservationService;
  @Mock private IdempotencyGuardPort idempotencyGuard;
  @Mock private MessagingErrorHandlerPort messagingErrorHandler;

  @InjectMocks private StockReservationConsumer consumer;

  private static final OrderCreatedEvent EVENT =
      new OrderCreatedEvent(
          1L,
          1L,
          new BigDecimal("200.00"),
          List.of(new OrderCreatedEvent.OrderItemPayload(1L, 2)),
          LocalDateTime.now());

  @Test
  @DisplayName(
      "onOrderCreated — should reserve stock and mark message as processed when not a duplicate")
  void onOrderCreated_shouldProcessNewMessage() {
    when(idempotencyGuard.isAlreadyProcessed("msg-1")).thenReturn(false);

    consumer.onOrderCreated(EVENT, "msg-1");

    verify(stockReservationService).reserveStock(EVENT);
    verify(idempotencyGuard).markProcessed("msg-1");
    verify(messagingErrorHandler, never()).resolve(any(), any());
  }

  @Test
  @DisplayName("onOrderCreated — should skip processing when the message was already processed")
  void onOrderCreated_shouldSkipDuplicateMessage() {
    when(idempotencyGuard.isAlreadyProcessed("msg-1")).thenReturn(true);

    consumer.onOrderCreated(EVENT, "msg-1");

    verify(stockReservationService, never()).reserveStock(any());
    verify(idempotencyGuard, never()).markProcessed(any());
  }

  @Test
  @DisplayName(
      "onOrderCreated — should throw AmqpRejectAndDontRequeueException when chain resolves REJECT_TO_DEAD_LETTER")
  void onOrderCreated_shouldRejectToDeadLetterWhenChainResolvesSo() {
    RuntimeException failure = new RuntimeException("order not found");
    when(idempotencyGuard.isAlreadyProcessed("msg-1")).thenReturn(false);
    doThrow(failure).when(stockReservationService).reserveStock(EVENT);
    when(messagingErrorHandler.resolve(eq(failure), any()))
        .thenReturn(MessageErrorResolution.rejectToDeadLetter("permanent error"));

    assertThatThrownBy(() -> consumer.onOrderCreated(EVENT, "msg-1"))
        .isInstanceOf(AmqpRejectAndDontRequeueException.class);

    verify(idempotencyGuard, never()).markProcessed("msg-1");
  }

  @Test
  @DisplayName(
      "onOrderCreated — should throw MessageProcessingRetryableException when chain resolves RETRY")
  void onOrderCreated_shouldRetryWhenChainResolvesSo() {
    DataAccessResourceFailureException failure = new DataAccessResourceFailureException("db down");
    when(idempotencyGuard.isAlreadyProcessed("msg-1")).thenReturn(false);
    doThrow(failure).when(stockReservationService).reserveStock(EVENT);
    when(messagingErrorHandler.resolve(eq(failure), any()))
        .thenReturn(MessageErrorResolution.retry("transient error"));

    assertThatThrownBy(() -> consumer.onOrderCreated(EVENT, "msg-1"))
        .isInstanceOf(MessageProcessingRetryableException.class);

    verify(idempotencyGuard, never()).markProcessed("msg-1");
  }

  @Test
  @DisplayName(
      "onOrderCreated — should mark message as processed without rethrowing when chain resolves ACKNOWLEDGE")
  void onOrderCreated_shouldAcknowledgeWhenChainResolvesSo() {
    RuntimeException failure = new RuntimeException("already handled by the saga");
    when(idempotencyGuard.isAlreadyProcessed("msg-1")).thenReturn(false);
    doThrow(failure).when(stockReservationService).reserveStock(EVENT);
    when(messagingErrorHandler.resolve(eq(failure), any()))
        .thenReturn(MessageErrorResolution.acknowledge("already compensated"));

    assertThatCode(() -> consumer.onOrderCreated(EVENT, "msg-1")).doesNotThrowAnyException();

    verify(idempotencyGuard).markProcessed("msg-1");
  }
}
