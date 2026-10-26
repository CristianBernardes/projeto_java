package br.com.projetojava.shared.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.projetojava.shared.exception.http4xx.BadRequestException;
import br.com.projetojava.shared.exception.http4xx.ConflictException;
import br.com.projetojava.shared.exception.http4xx.ForbiddenException;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.shared.exception.http4xx.UnauthorizedException;
import br.com.projetojava.shared.exception.http4xx.UnprocessableEntityException;
import br.com.projetojava.shared.exception.http5xx.ServiceUnavailableException;
import br.com.projetojava.shared.handler.chain.ApiExceptionHandler;
import br.com.projetojava.shared.handler.chain.FallbackExceptionHandler;
import br.com.projetojava.shared.handler.chain.SpringWebExceptionHandler;
import br.com.projetojava.shared.handler.chain.ValidationExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("GlobalExceptionHandler — Chain of Responsibility")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler =
        new GlobalExceptionHandler(
            new ApiExceptionHandler(),
            new ValidationExceptionHandler(),
            new SpringWebExceptionHandler(),
            new FallbackExceptionHandler());
    handler.buildChain();

    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/v1/products/1");
    when(request.getMethod()).thenReturn("GET");
  }

  @Test
  @DisplayName("ResourceNotFoundException → 404")
  void shouldReturn404() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new ResourceNotFoundException("Product not found."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(404);
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().message()).isEqualTo("Product not found.");
  }

  @Test
  @DisplayName("BadRequestException → 400")
  void shouldReturn400() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new BadRequestException("Invalid request."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  @DisplayName("UnauthorizedException → 401")
  void shouldReturn401() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new UnauthorizedException("Not authenticated."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(401);
  }

  @Test
  @DisplayName("ForbiddenException → 403")
  void shouldReturn403() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new ForbiddenException("Access denied."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(403);
  }

  @Test
  @DisplayName("ConflictException → 409")
  void shouldReturn409() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new ConflictException("Resource already exists."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  @DisplayName("UnprocessableEntityException with field errors → 422 with error list")
  void shouldReturn422WithErrors() {
    List<String> errors = List.of("name: Name is required.", "price: Price must be positive.");
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new UnprocessableEntityException("Invalid data.", errors), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().errors()).hasSize(2);
  }

  @Test
  @DisplayName("ServiceUnavailableException → 503")
  void shouldReturn503() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new ServiceUnavailableException("Service unavailable."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(503);
  }

  @Test
  @DisplayName("Generic RuntimeException → 500 via FallbackExceptionHandler")
  void shouldReturn500ForGenericException() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new RuntimeException("Unexpected error."), request);
    assertThat(resp.getStatusCode().value()).isEqualTo(500);
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().message()).contains("internal error");
  }

  @Test
  @DisplayName("ErrorResponse should contain the request path")
  void shouldContainPathInResponse() {
    ResponseEntity<ErrorResponse> resp =
        handler.handle(new ResourceNotFoundException("Not found."), request);
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().path()).isEqualTo("/api/v1/products/1");
  }
}
