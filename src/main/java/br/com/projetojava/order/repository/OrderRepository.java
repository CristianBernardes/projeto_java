package br.com.projetojava.order.repository;

import br.com.projetojava.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for the {@link Order} aggregate root.
 *
 * <p>Order items are managed exclusively through the {@link Order} aggregate (cascade), so no
 * separate repository is exposed for {@code OrderItem}.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {}
