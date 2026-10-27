package br.com.projetojava.outbox.repository;

import br.com.projetojava.outbox.domain.OutboxEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link OutboxEvent}.
 *
 * <p>Used by the outbox poller to find unpublished events in creation order (FIFO), bounded by a
 * batch size to avoid loading unbounded result sets.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  /**
   * Finds unpublished events ordered by creation time, limited by the given page size.
   *
   * @param pageable pagination/limit configuration (use {@code PageRequest.of(0, batchSize)})
   * @return list of unpublished events, oldest first
   */
  List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc(Pageable pageable);
}
