package br.com.projetojava.outbox.service;

import br.com.projetojava.order.event.OrderCreatedEvent;
import br.com.projetojava.order.messaging.OrderEventPublisherPort;
import br.com.projetojava.order.messaging.OrderMessagePublisherPort;
import br.com.projetojava.outbox.domain.OutboxEvent;
import br.com.projetojava.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publishes a single outbox event to the configured message brokers.
 *
 * <p>Extracted into its own Spring bean (rather than a method on {@link OutboxEventPublisher}) so
 * that the {@link Transactional @Transactional} boundary around each event is honored — calling a
 * {@code @Transactional} method on {@code this} from within the same class bypasses the Spring
 * proxy and silently skips the transaction.
 *
 * <p>Implements {@link SingleOutboxEventPublisherPort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class SingleOutboxEventPublisher implements SingleOutboxEventPublisherPort {

  private static final String EVENT_TYPE_ORDER_CREATED = "order.created";

  private final OutboxEventRepository outboxEventRepository;
  private final OrderMessagePublisherPort rabbitOrderMessagePublisher;
  private final OrderEventPublisherPort kafkaOrderEventPublisher;
  private final ObjectMapper objectMapper;

  /**
   * Publishes a single outbox event in its own transaction, so one failure does not roll back or
   * block the rest of the batch being processed by the poller.
   *
   * @param event outbox event to publish
   */
  @Override
  @Transactional
  public void publish(OutboxEvent event) {
    try {
      Object payload = deserialize(event);

      if (EVENT_TYPE_ORDER_CREATED.equals(event.getEventType())) {
        rabbitOrderMessagePublisher.publishOrderCreated("outbox-" + event.getId(), payload);
      }
      kafkaOrderEventPublisher.publish(event.getAggregateId(), event.getEventType(), payload);

      event.markPublished();
      outboxEventRepository.save(event);
    } catch (Exception e) {
      log.error(
          "Falha ao publicar evento do outbox id={}, type='{}'. Haverá nova tentativa no próximo"
              + " poll.",
          event.getId(),
          event.getEventType(),
          e);
    }
  }

  private Object deserialize(OutboxEvent event) throws Exception {
    return switch (event.getEventType()) {
      case EVENT_TYPE_ORDER_CREATED ->
          objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);
      default -> objectMapper.readValue(event.getPayload(), Object.class);
    };
  }
}
