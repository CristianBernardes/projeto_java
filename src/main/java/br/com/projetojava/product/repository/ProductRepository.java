package br.com.projetojava.product.repository;

import br.com.projetojava.product.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
