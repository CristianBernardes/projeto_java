package br.com.projetojava.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.dto.CreateProductRequest;
import br.com.projetojava.product.dto.ProductResponse;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  @Test
  @DisplayName("create — should persist and return the created product")
  void create_shouldPersistAndReturnProduct() {
    CreateProductRequest request =
        new CreateProductRequest("Keyboard", "Mechanical", new BigDecimal("299.90"), 10);

    Product product = Product.create("Keyboard", "Mechanical", new BigDecimal("299.90"), 10);
    when(productRepository.save(any(Product.class))).thenReturn(product);

    ProductResponse response = productService.create(request);

    assertThat(response.name()).isEqualTo("Keyboard");
    assertThat(response.price()).isEqualByComparingTo("299.90");
    assertThat(response.stock()).isEqualTo(10);
    verify(productRepository).save(any(Product.class));
  }

  @Test
  @DisplayName("findById — should throw ResourceNotFoundException when not found")
  void findById_shouldThrowWhenNotFound() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  @DisplayName("deactivate — should call deactivate on entity and save")
  void deactivate_shouldCallDeactivateAndSave() {
    Product product = Product.create("Mouse", null, new BigDecimal("89.90"), 5);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(product)).thenReturn(product);

    productService.deactivate(1L);

    assertThat(product.isActive()).isFalse();
    verify(productRepository).save(product);
  }
}
