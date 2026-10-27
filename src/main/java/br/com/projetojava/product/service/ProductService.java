package br.com.projetojava.product.service;

import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.dto.CreateProductRequest;
import br.com.projetojava.product.dto.ProductResponse;
import br.com.projetojava.product.dto.UpdateProductRequest;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the product domain use cases.
 *
 * <p>Orchestrates operations between the controller and the repository, delegating business rules
 * to the {@link Product} entity.
 *
 * <p>Implements {@link ProductServicePort} to respect the <em>Dependency Inversion Principle</em>:
 * consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductServicePort {

  private final ProductRepository productRepository;

  /**
   * Lists all active products.
   *
   * @return list of response DTOs for active products
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProductResponse> listActive() {
    log.info("Listing active products.");
    return productRepository.findAllByActive(true).stream().map(ProductResponse::from).toList();
  }

  /**
   * Finds a product by its identifier.
   *
   * @param id product identifier
   * @return response DTO of the found product
   * @throws ResourceNotFoundException if the product does not exist
   */
  @Override
  @Transactional(readOnly = true)
  public ProductResponse findById(Long id) {
    log.info("Finding product id={}.", id);
    return ProductResponse.from(findEntity(id));
  }

  /**
   * Creates a new product from the provided data.
   *
   * @param request creation data
   * @return response DTO of the created product
   */
  @Override
  @Transactional
  public ProductResponse create(CreateProductRequest request) {
    log.info("Creating product name='{}'.", request.name());
    Product product =
        Product.create(request.name(), request.description(), request.price(), request.stock());
    return ProductResponse.from(productRepository.save(product));
  }

  /**
   * Updates name, description, price and stock of an existing product.
   *
   * @param id product identifier
   * @param request new data
   * @return response DTO with updated data
   * @throws ResourceNotFoundException if the product does not exist
   */
  @Override
  @Transactional
  public ProductResponse update(Long id, UpdateProductRequest request) {
    log.info("Updating product id={}.", id);
    Product product = findEntity(id);
    product.setName(request.name());
    product.setDescription(request.description());
    product.updatePrice(request.price());
    product.updateStock(request.stock());
    return ProductResponse.from(productRepository.save(product));
  }

  /**
   * Deactivates a product, making it unavailable in the catalog.
   *
   * @param id product identifier
   * @throws ResourceNotFoundException if the product does not exist
   */
  @Override
  @Transactional
  public void deactivate(Long id) {
    log.info("Deactivating product id={}.", id);
    Product product = findEntity(id);
    product.deactivate();
    productRepository.save(product);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private Product findEntity(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found. id=" + id));
  }
}
