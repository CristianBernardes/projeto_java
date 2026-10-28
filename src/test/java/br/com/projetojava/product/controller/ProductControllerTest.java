package br.com.projetojava.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.projetojava.product.dto.CreateProductRequest;
import br.com.projetojava.product.dto.ProductResponse;
import br.com.projetojava.product.service.ProductServicePort;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.shared.handler.GlobalExceptionHandler;
import br.com.projetojava.shared.handler.chain.ApiExceptionHandler;
import br.com.projetojava.shared.handler.chain.FallbackExceptionHandler;
import br.com.projetojava.shared.handler.chain.SpringWebExceptionHandler;
import br.com.projetojava.shared.handler.chain.ValidationExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link ProductController} using MockMvc.
 *
 * <p>Spring Security auto-configuration is excluded so that the web layer can be tested in
 * isolation without needing a JWT secret or a running security filter chain. Authentication and
 * authorisation rules are covered by dedicated integration tests.
 */
@WebMvcTest(
    controllers = ProductController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    },
    excludeFilters = {
      @org.springframework.context.annotation.ComponentScan.Filter(
          type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
          classes = br.com.projetojava.auth.security.JwtAuthFilter.class)
    })
@Import({
  GlobalExceptionHandler.class,
  ApiExceptionHandler.class,
  ValidationExceptionHandler.class,
  SpringWebExceptionHandler.class,
  FallbackExceptionHandler.class
})
@DisplayName("ProductController")
class ProductControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ProductServicePort productService;

  @Test
  @DisplayName("POST /api/v1/products — should return 201 with created product")
  void create_shouldReturn201() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest("Keyboard", "Mechanical", new BigDecimal("299.90"), 10);

    ProductResponse response =
        new ProductResponse(
            1L,
            "Keyboard",
            "Mechanical",
            new BigDecimal("299.90"),
            10,
            true,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(productService.create(any(CreateProductRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Keyboard"));
  }

  @Test
  @DisplayName("GET /api/v1/products/{id} — should return 404 when not found")
  void findById_shouldReturn404() throws Exception {
    when(productService.findById(99L))
        .thenThrow(new ResourceNotFoundException("Produto não encontrado. id=99"));

    mockMvc
        .perform(get("/api/v1/products/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("POST /api/v1/products — should return 422 when payload is invalid")
  void create_shouldReturn422WhenPayloadInvalid() throws Exception {
    String invalidPayload =
        """
        {"name": "", "price": -1, "stock": -5}
        """;

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").isArray());
  }
}
