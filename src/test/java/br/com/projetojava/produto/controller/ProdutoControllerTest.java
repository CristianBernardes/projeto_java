package br.com.projetojava.produto.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.projetojava.produto.dto.CriarProdutoRequest;
import br.com.projetojava.produto.dto.ProdutoResponse;
import br.com.projetojava.produto.service.ProdutoServicePort;
import br.com.projetojava.shared.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProdutoController.class)
@DisplayName("ProdutoController")
class ProdutoControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ProdutoServicePort produtoService;

  @Test
  @DisplayName("POST /api/v1/produtos — deve retornar 201 com o produto criado")
  void criar_deveRetornar201() throws Exception {
    CriarProdutoRequest request =
        new CriarProdutoRequest("Teclado", "Mecânico", new BigDecimal("299.90"), 10);

    ProdutoResponse response =
        new ProdutoResponse(
            1L,
            "Teclado",
            "Mecânico",
            new BigDecimal("299.90"),
            10,
            true,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(produtoService.criar(any(CriarProdutoRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.nome").value("Teclado"));
  }

  @Test
  @DisplayName("GET /api/v1/produtos/{id} — deve retornar 404 quando não encontrado")
  void buscarPorId_deveRetornar404() throws Exception {
    when(produtoService.buscarPorId(99L))
        .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado. id=99"));

    mockMvc
        .perform(get("/api/v1/produtos/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("POST /api/v1/produtos — deve retornar 422 quando payload inválido")
  void criar_deveRetornar422QuandoPayloadInvalido() throws Exception {
    String payloadInvalido =
        """
        {"nome": "", "preco": -1, "estoque": -5}
        """;

    mockMvc
        .perform(
            post("/api/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadInvalido))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.detalhes").isArray());
  }
}
