package br.com.projetojava.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.dto.UserResponse;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.upload.dto.FileUploadResponse;
import br.com.projetojava.upload.service.FileUploadServicePort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService")
class UserProfileServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private FileUploadServicePort fileUploadService;

  @InjectMocks private UserProfileService userProfileService;

  @Test
  @DisplayName("getProfile — should return profile without avatar info when none is set")
  void getProfile_shouldReturnProfileWithoutAvatar() {
    User user = User.create("user@example.com", "$2a$hash", "USER");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserResponse response = userProfileService.getProfile(1L);

    assertThat(response.email()).isEqualTo("user@example.com");
    assertThat(response.avatarId()).isNull();
    assertThat(response.avatarDownloadUrl()).isNull();
  }

  @Test
  @DisplayName("getProfile — should include avatar info when avatar is set")
  void getProfile_shouldIncludeAvatarInfoWhenSet() {
    User user = User.create("user@example.com", "$2a$hash", "USER");
    user.updateAvatar(7L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserResponse response = userProfileService.getProfile(1L);

    assertThat(response.avatarId()).isEqualTo(7L);
    assertThat(response.avatarDownloadUrl()).isEqualTo("/api/v1/files/7");
  }

  @Test
  @DisplayName("getProfile — should throw ResourceNotFoundException when user does not exist")
  void getProfile_shouldThrowWhenUserNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userProfileService.getProfile(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("uploadAvatar — should store file and update the user's avatar pointer")
  void uploadAvatar_shouldStoreFileAndUpdatePointer() {
    User user = User.create("user@example.com", "$2a$hash", "USER");
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "img-bytes".getBytes());
    FileUploadResponse upload =
        new FileUploadResponse(5L, "avatar.png", "image/png", 9L, LocalDateTime.now());

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(fileUploadService.store(file, null, 1L)).thenReturn(upload);
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    UserResponse response = userProfileService.uploadAvatar(1L, file);

    assertThat(response.avatarId()).isEqualTo(5L);
    assertThat(response.avatarDownloadUrl()).isEqualTo("/api/v1/files/5");
    assertThat(user.getAvatarId()).isEqualTo(5L);
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("uploadAvatar — should throw ResourceNotFoundException when user does not exist")
  void uploadAvatar_shouldThrowWhenUserNotFound() {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "img-bytes".getBytes());
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userProfileService.uploadAvatar(99L, file))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(fileUploadService, never()).store(any(), any(), any());
  }
}
