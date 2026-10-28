package br.com.projetojava.order.controller;

import br.com.projetojava.order.dto.CreateOrderRequest;
import br.com.projetojava.order.dto.OrderResponse;
import br.com.projetojava.order.service.OrderServicePort;
import br.com.projetojava.shared.security.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for the {@code /api/v1/orders} resource.
 *
 * <p>Order creation is <strong>asynchronous</strong>: {@code POST} returns {@code 202 Accepted}
 * immediately with the order in {@code PENDING} status. Stock reservation happens in the background
 * via RabbitMQ (see the {@code order.messaging} package); clients should poll {@code GET /{id}} to
 * observe the final {@code CONFIRMED} or {@code FAILED} status.
 */
@Tag(
    name = "Pedidos",
    description = "Criação assíncrona de pedidos com reserva de estoque em segundo plano")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderServicePort orderService;
  private final AuthenticatedUserResolver authenticatedUserResolver;

  /**
   * Creates a new order for the authenticated user.
   *
   * <p>The response status is {@code 202 Accepted}, not {@code 201 Created}: the order has been
   * accepted for processing, but stock has not been reserved yet — that happens asynchronously.
   *
   * @param request validated order creation data
   * @param authentication current authenticated user
   * @return {@code 202 Accepted} with the order in {@code PENDING} status and a {@code Location}
   *     header pointing to the status endpoint
   */
  @Operation(
      summary = "Criar pedido",
      description =
          "Aceita um novo pedido para processamento assíncrono. O pedido começa com status"
              + " PENDING; o estoque é reservado em segundo plano via uma fila de mensagens. Faça"
              + " polling em GET /{id} para observar o status final CONFIRMED ou FAILED.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "202",
            description = "Pedido aceito para processamento (status: PENDING)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "Um produto referenciado não existe",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "422",
            description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping
  public ResponseEntity<OrderResponse> create(
      @Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
    OrderResponse created = orderService.create(request, userId);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.accepted().location(location).body(created);
  }

  /**
   * Finds an order by its identifier, including its current processing status.
   *
   * @param id order identifier
   * @return {@code 200 OK} with the order, or {@code 404} if not found
   */
  @Operation(
      summary = "Buscar pedido por ID",
      description =
          "Retorna um pedido pelo seu identificador único, incluindo seu status atual"
              + " (PENDING, CONFIRMED, FAILED ou CANCELLED). Requer autenticação.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "Pedido não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> findById(
      @Parameter(description = "Identificador do pedido", example = "1", required = true)
          @PathVariable
          Long id) {
    return ResponseEntity.ok(orderService.findById(id));
  }
}
