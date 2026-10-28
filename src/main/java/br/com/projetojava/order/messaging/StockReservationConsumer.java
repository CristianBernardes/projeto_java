package br.com.projetojava.order.messaging;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import br.com.projetojava.messaging.errorhandling.MessagingErrorHandlerPort;
import br.com.projetojava.messaging.idempotency.IdempotencyGuardPort;
import br.com.projetojava.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter that consumes {@code order.created} messages from the stock reservation work
 * queue and delegates to {@link StockReservationServicePort}.
 *
 * <p>RabbitMQ/Kafka deliver messages <strong>at-least-once</strong>: a broker restart, consumer
 * crash, or network retry can redeliver the same message. {@link IdempotencyGuardPort} ensures a
 * redelivered message is a no-op instead of debiting stock twice.
 *
 * <p>Any exception raised by {@link StockReservationServicePort} is routed through {@link
 * MessagingErrorHandlerPort} — the Chain of Responsibility that decides, per exception type,
 * whether the message should be retried, rejected straight to the dead-letter queue, or
 * acknowledged. This mirrors how {@code GlobalExceptionHandler} is the single place HTTP exceptions
 * are translated into a response: here, the chain is the single place a processing failure is
 * translated into a RabbitMQ ack/retry/reject decision.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationConsumer {

  private static final String QUEUE_NAME = "${app.messaging.rabbitmq.order-created-queue}";

  private final StockReservationServicePort stockReservationService;
  private final IdempotencyGuardPort idempotencyGuard;
  private final MessagingErrorHandlerPort messagingErrorHandler;

  /**
   * Consumes an {@code order.created} message and reserves stock for its items.
   *
   * @param event order creation event carrying the items to reserve stock for
   * @param messageId unique identifier of the incoming message, used for the idempotency check
   */
  @RabbitListener(queues = QUEUE_NAME)
  public void onOrderCreated(
      OrderCreatedEvent event, @Header(AmqpHeaders.MESSAGE_ID) String messageId) {
    if (idempotencyGuard.isAlreadyProcessed(messageId)) {
      log.info("Mensagem id='{}' já processada; ignorando (reentrega at-least-once).", messageId);
      return;
    }

    try {
      log.info("Reservando estoque para order_id={}, message_id='{}'.", event.orderId(), messageId);
      stockReservationService.reserveStock(event);
      idempotencyGuard.markProcessed(messageId);
    } catch (Exception ex) {
      handleFailure(ex, messageId);
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  /**
   * Routes a processing failure through the message error handling chain and acts on the resulting
   * decision.
   *
   * @param ex exception raised while processing the message
   * @param messageId identifier of the message that failed, for logging/context
   */
  private void handleFailure(Exception ex, String messageId) {
    MessageErrorContext context =
        new MessageErrorContext("rabbitmq.order.created.queue", messageId);
    MessageErrorResolution resolution = messagingErrorHandler.resolve(ex, context);

    switch (resolution.action()) {
      case ACKNOWLEDGE -> {
        log.info(
            "Mensagem id='{}' resolvida como ACKNOWLEDGE: {}. Nenhuma ação adicional.",
            messageId,
            resolution.reason());
        idempotencyGuard.markProcessed(messageId);
      }
      case REJECT_TO_DEAD_LETTER ->
          // Throwing this (unlike a plain exception) tells Spring AMQP to never requeue the
          // message, sending it straight to the dead-letter exchange configured on the queue.
          throw new AmqpRejectAndDontRequeueException(resolution.reason(), ex);
      case RETRY ->
          // Re-throwing a plain exception lets the listener container's configured retry
          // interceptor (spring.rabbitmq.listener.simple.retry) handle backoff and eventual
          // dead-lettering after the max attempts are exhausted.
          throw new MessageProcessingRetryableException(resolution.reason(), ex);
    }
  }
}
