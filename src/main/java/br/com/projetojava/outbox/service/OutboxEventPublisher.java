package br.com.projetojava.outbox.service;

import br.com.projetojava.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reader-side (poller) of the Transactional Outbox pattern.
 *
 * <p>Periodically scans {@code outbox_events} for unpublished rows and delegates each one to {@link
 * SingleOutboxEventPublisher}, which relays it to the message brokers:
 *
 * <ul>
 *   <li><strong>RabbitMQ</strong> — {@code order.created} events go to the stock reservation work
 *       queue (exactly one consumer processes each order)
 *   <li><strong>Kafka</strong> — every event is also published to the {@code order-events} topic
 *       for auditing/streaming, regardless of type
 * </ul>
 *
 * <p>Each event is published in its own transaction (see {@link SingleOutboxEventPublisher}): if
 * publishing one event fails, it stays unpublished and is retried on the next poll, without
 * blocking the others in the batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

  @Value("${app.messaging.outbox.batch-size}")
  private int batchSize;

  private final OutboxEventRepository outboxEventRepository;
  private final SingleOutboxEventPublisherPort singleOutboxEventPublisher;

  /**
   * Polls for unpublished outbox events and relays each one to the configured brokers.
   *
   * <p>The poll interval is configured via {@code app.messaging.outbox.poll-interval-ms}.
   */
  @Scheduled(fixedDelayString = "${app.messaging.outbox.poll-interval-ms}")
  public void publishPendingEvents() {
    var pending =
        outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc(PageRequest.of(0, batchSize));

    if (pending.isEmpty()) {
      return;
    }

    log.info("Publicando {} evento(s) pendente(s) do outbox.", pending.size());
    pending.forEach(singleOutboxEventPublisher::publish);
  }
}
