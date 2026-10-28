package br.com.projetojava.auth.service;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.dto.UserResponse;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.upload.dto.FileUploadResponse;
import br.com.projetojava.upload.service.FileUploadServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of authenticated user profile use cases.
 *
 * <p>Delegates the actual file storage and validation to {@link FileUploadServicePort} — this class
 * only owns the business rule of pointing {@code users.avatar_id} at the resulting upload, avoiding
 * duplicating upload logic (DRY).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileServicePort {

  private final UserRepository userRepository;
  private final FileUploadServicePort fileUploadService;

  /**
   * {@inheritDoc}
   *
   * @throws ResourceNotFoundException if the user does not exist
   */
  @Override
  @Transactional(readOnly = true)
  public UserResponse getProfile(Long userId) {
    User user = findUser(userId);
    return toResponse(user);
  }

  /**
   * {@inheritDoc}
   *
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if the file type is not
   *     allowed or the file exceeds the size limit
   * @throws ResourceNotFoundException if the user does not exist
   */
  @Override
  @Transactional
  public UserResponse uploadAvatar(Long userId, MultipartFile file) {
    User user = findUser(userId);

    FileUploadResponse upload = fileUploadService.store(file, null, userId);
    user.updateAvatar(upload.id());
    userRepository.save(user);

    log.info("Avatar atualizado para o usuário id={}, file_upload_id={}.", userId, upload.id());
    return toResponse(user);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private User findUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. id=" + userId));
  }

  private UserResponse toResponse(User user) {
    Long avatarId = user.getAvatarId();
    String avatarDownloadUrl = avatarId != null ? "/api/v1/files/" + avatarId : null;
    return new UserResponse(
        user.getId(), user.getEmail(), user.getRole(), avatarId, avatarDownloadUrl);
  }
}
