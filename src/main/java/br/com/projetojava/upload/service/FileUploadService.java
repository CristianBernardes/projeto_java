package br.com.projetojava.upload.service;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.BadRequestException;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.upload.domain.FileUpload;
import br.com.projetojava.upload.dto.FileUploadResponse;
import br.com.projetojava.upload.repository.FileUploadRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of file upload use cases.
 *
 * <p>Stores files on disk under the configured upload directory and persists metadata in the
 * database. Each file is linked to a {@link Product} and the {@link User} who uploaded it. File
 * names are UUID-based to avoid collisions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService implements FileUploadServicePort {

  private static final List<String> ALLOWED_TYPES =
      List.of("image/jpeg", "image/png", "application/pdf");

  @Value("${app.upload.directory}")
  private String uploadDirectory;

  @Value("${app.upload.max-size-bytes}")
  private long maxSizeBytes;

  private final FileUploadRepository fileUploadRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  /**
   * Ensures the upload directory exists on application startup.
   *
   * @throws RuntimeException if the directory cannot be created
   */
  @PostConstruct
  void initDirectory() {
    try {
      Files.createDirectories(Paths.get(uploadDirectory));
      log.info("Upload directory ready: {}", uploadDirectory);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create upload directory: " + uploadDirectory, e);
    }
  }

  /**
   * Stores an uploaded file and optionally links it to a product and/or a user.
   *
   * @param file multipart file from the HTTP request
   * @param productId ID of the product this file belongs to, or {@code null}
   * @param userId ID of the authenticated user performing the upload, or {@code null}
   * @return response DTO with file metadata
   * @throws BadRequestException if the file type is not allowed or exceeds the size limit
   * @throws ResourceNotFoundException if the given product or user ID does not exist
   */
  @Override
  @Transactional
  public FileUploadResponse store(MultipartFile file, Long productId, Long userId) {
    validateFile(file);

    Product product = null;
    if (productId != null) {
      product =
          productRepository
              .findById(productId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Product not found. id=" + productId));
    }

    User user = null;
    if (userId != null) {
      user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + userId));
    }

    String originalName = file.getOriginalFilename();
    String extension = extractExtension(originalName);
    String storedName = UUID.randomUUID() + extension;
    Path destination = Paths.get(uploadDirectory).resolve(storedName);

    try {
      Files.copy(file.getInputStream(), destination);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file: " + originalName, e);
    }

    FileUpload upload =
        FileUpload.create(
            originalName,
            storedName,
            file.getContentType(),
            file.getSize(),
            destination.toString(),
            product,
            user);

    FileUpload saved = fileUploadRepository.save(upload);
    log.info(
        "File stored: id={}, name='{}', product_id={}, user_id={}.",
        saved.getId(),
        storedName,
        productId,
        userId);

    return toResponse(saved);
  }

  /**
   * Sets the given file upload as the active avatar for a user.
   *
   * <p>The previous avatar is not deleted — it remains in {@code file_uploads} as history.
   *
   * @param fileId ID of the {@link FileUpload} to use as avatar
   * @param userId ID of the user whose avatar will be updated
   * @throws ResourceNotFoundException if the file or user does not exist
   */
  @Override
  @Transactional
  public void setUserAvatar(Long fileId, Long userId) {
    FileUpload file =
        fileUploadRepository
            .findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found. id=" + fileId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + userId));

    user.updateAvatar(file);
    userRepository.save(user);
    log.info("Avatar updated: user_id={}, file_id={}.", userId, fileId);
  }

  /**
   * Loads a stored file as a {@link Resource} for download.
   *
   * @param id file upload record identifier
   * @return the file as a {@link Resource}
   * @throws ResourceNotFoundException if the record does not exist
   */
  @Override
  @Transactional(readOnly = true)
  public Resource load(Long id) {
    FileUpload upload =
        fileUploadRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("File not found. id=" + id));

    try {
      Path filePath = Paths.get(upload.getPath());
      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new ResourceNotFoundException("File not readable. id=" + id);
      }
      return resource;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load file. id=" + id, e);
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("File must not be empty.");
    }
    if (file.getSize() > maxSizeBytes) {
      throw new BadRequestException(
          "File exceeds maximum allowed size of %d bytes.".formatted(maxSizeBytes));
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BadRequestException(
          "File type not allowed: '%s'. Allowed: %s.".formatted(contentType, ALLOWED_TYPES));
    }
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.'));
  }

  private FileUploadResponse toResponse(FileUpload upload) {
    return new FileUploadResponse(
        upload.getId(),
        upload.getOriginalName(),
        upload.getContentType(),
        upload.getSizeBytes(),
        upload.getProduct() != null ? upload.getProduct().getId() : null,
        upload.getUploadedBy() != null ? upload.getUploadedBy().getId() : null,
        upload.getCreatedAt());
  }
}
