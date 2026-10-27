package br.com.projetojava.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.projetojava.auth.dto.UserResponse;
import br.com.projetojava.auth.service.UserProfileServicePort;
import br.com.projetojava.shared.exception.http4xx.BadRequestException;
import br.com.projetojava.shared.handler.GlobalExceptionHandler;
import br.com.projetojava.shared.handler.chain.ApiExceptionHandler;
import br.com.projetojava.shared.handler.chain.FallbackExceptionHandler;
import br.com.projetojava.shared.handler.chain.SpringWebExceptionHandler;
import br.com.projetojava.shared.handler.chain.ValidationExceptionHandler;
import br.com.projetojava.shared.security.AuthenticatedUserResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link UserController} using MockMvc.
 *
 * <p>Spring Security auto-configuration is excluded so the web layer can be tested in isolation.
 * {@link AuthenticatedUserResolver} is mocked directly to simulate an already-authenticated
 * principal without needing a real JWT or security filter chain.
 */
@WebMvcTest(
    controllers = UserController.class,
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
@DisplayName("UserController")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserProfileServicePort userProfileService;
  @MockBean private AuthenticatedUserResolver authenticatedUserResolver;

  @Test
  @DisplayName("GET /api/v1/users/me — should return 200 with the profile")
  void me_shouldReturn200() throws Exception {
    UserResponse response = new UserResponse(1L, "user@example.com", "USER", null, null);
    when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
    when(userProfileService.getProfile(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("user@example.com"))
        .andExpect(jsonPath("$.avatarId").doesNotExist());
  }

  @Test
  @DisplayName("POST /api/v1/users/me/avatar — should return 200 with the updated profile")
  void uploadAvatar_shouldReturn200() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "img-bytes".getBytes());
    UserResponse response = new UserResponse(1L, "user@example.com", "USER", 5L, "/api/v1/files/5");

    when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
    when(userProfileService.uploadAvatar(eq(1L), any())).thenReturn(response);

    mockMvc
        .perform(multipart("/api/v1/users/me/avatar").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.avatarId").value(5))
        .andExpect(jsonPath("$.avatarDownloadUrl").value("/api/v1/files/5"));
  }

  @Test
  @DisplayName("POST /api/v1/users/me/avatar — should return 400 when file type is not allowed")
  void uploadAvatar_shouldReturn400WhenFileTypeNotAllowed() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "virus.exe", "application/x-msdownload", "x".getBytes());

    when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
    when(userProfileService.uploadAvatar(eq(1L), any()))
        .thenThrow(new BadRequestException("File type not allowed."));

    mockMvc
        .perform(multipart("/api/v1/users/me/avatar").file(file))
        .andExpect(status().isBadRequest());
  }
}
