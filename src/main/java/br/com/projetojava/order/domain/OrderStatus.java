package br.com.projetojava.order.domain;

/**
 * Lifecycle states of an {@link Order}.
 *
 * <p>Transitions are driven by the asynchronous order processing saga:
 *
 * <pre>
 * PENDING --(stock reserved)--&gt; CONFIRMED
 * PENDING --(insufficient stock)--&gt; FAILED
 * PENDING --(explicit cancellation)--&gt; CANCELLED
 * </pre>
 */
public enum OrderStatus {

  /** Order was accepted and persisted; stock reservation is still pending (async). */
  PENDING,

  /** Stock was successfully reserved for every item; the order is confirmed. */
  CONFIRMED,

  /** Stock reservation failed for at least one item; the saga compensated the order. */
  FAILED,

  /** Order was explicitly cancelled before being confirmed. */
  CANCELLED
}
