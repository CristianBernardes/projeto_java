package br.com.projetojava.outbox.service;

import br.com.projetojava.outbox.domain.OutboxEvent;
import br.com.projetojava.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Writer-side of the Transactional Outbox pattern.
 *
 * <p>Call {@link #append} from within the <em>same transaction</em> as the business change the
 * event represents (e.g., inside {@code OrderService.create}, right after {@code
 * orderRepository.save(order)}). Because both writes share one transaction, they either both commit
 * or both roll back together — there is no window where the order exists but the event does not, or
 * vice-versa.
 *
 * <p>Implements {@link OutboxEventServicePort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventService implements OutboxEventServicePort {

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  /**
   * Serializes the given payload as JSON and appends it as a new unpublished outbox event.
   *
   * @param aggregateType name of the aggregate that produced the event (e.g., {@code "Order"})
   * @param aggregateId identifier of the aggregate instance
   * @param eventType event type/name (e.g., {@code "order.created"})
   * @param payload event payload object, serialized to JSON for storage
   * @throws IllegalStateException if the payload cannot be serialized
   */
  @Override
  public void append(String aggregateType, Long aggregateId, String eventType, Object payload) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      outboxEventRepository.save(OutboxEvent.create(aggregateType, aggregateId, eventType, json));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(
          "Failed to serialize outbox event payload for aggregateType='%s', eventType='%s'."
              .formatted(aggregateType, eventType),
          e);
    }
  }
}
