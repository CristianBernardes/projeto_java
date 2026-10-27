package br.com.projetojava.product.repository;

import br.com.projetojava.product.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/**
 * JPA repository for the {@link Product} entity.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations. Custom queries are declared
 * here as derived methods or {@code @Query}.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Returns all products with the given active status.
   *
   * @param active {@code true} for active products, {@code false} for inactive
   * @return list of products filtered by status
   */
  List<Product> findAllByActive(boolean active);

  /**
   * Finds a product by id with a pessimistic write lock ({@code SELECT ... FOR UPDATE}).
   *
   * <p>Used by the asynchronous stock reservation consumer: concurrent orders for the same product
   * must not both read the same stock value and both succeed in decrementing it past zero. The row
   * lock serializes access so each order sees the up-to-date stock.
   *
   * @param id product identifier
   * @return locked product, if found
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findByIdForUpdate(Long id);
}
