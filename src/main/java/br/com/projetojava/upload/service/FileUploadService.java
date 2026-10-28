package br.com.projetojava.upload.service;

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
import java.util.stream.Collectors;
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
 * database. File names are UUID-based to avoid collisions.
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

  /**
   * Ensures the upload directory exists on application startup.
   *
   * @throws RuntimeException if the directory cannot be created
   */
  @PostConstruct
  void initDirectory() {
    try {
      Files.createDirectories(Paths.get(uploadDirectory));
      log.info("Diretório de upload pronto: {}", uploadDirectory);
    } catch (IOException e) {
      throw new RuntimeException("Falha ao criar o diretório de upload: " + uploadDirectory, e);
    }
  }

  /**
   * Stores an uploaded file and persists its metadata.
   *
   * @param file multipart file from the HTTP request
   * @param productId ID of the product this file belongs to (nullable)
   * @param userId ID of the authenticated user (nullable)
   * @return response DTO with file metadata
   * @throws BadRequestException if the file type is not allowed or exceeds the size limit
   */
  @Override
  @Transactional
  public FileUploadResponse store(MultipartFile file, Long productId, Long userId) {
    validateFile(file);

    String originalName = file.getOriginalFilename();
    String extension = extractExtension(originalName);
    String storedName = UUID.randomUUID() + extension;
    Path destination = Paths.get(uploadDirectory).resolve(storedName);

    try {
      Files.copy(file.getInputStream(), destination);
    } catch (IOException e) {
      throw new RuntimeException("Falha ao armazenar o arquivo: " + originalName, e);
    }

    FileUpload upload =
        FileUpload.create(
            originalName,
            storedName,
            file.getContentType(),
            file.getSize(),
            destination.toString(),
            productId,
            userId);

    FileUpload saved = fileUploadRepository.save(upload);
    log.info("Arquivo armazenado: id={}, name='{}'.", saved.getId(), storedName);

    return new FileUploadResponse(
        saved.getId(),
        saved.getOriginalName(),
        saved.getContentType(),
        saved.getSizeBytes(),
        saved.getCreatedAt());
  }

  /**
   * Stores multiple uploaded files and persists their metadata.
   *
   * <p>Used for product photos, where several images may belong to the same product. Each file is
   * validated and stored independently; if any file is invalid, the whole batch is rejected before
   * any file is written to disk.
   *
   * @param files multipart files from the HTTP request
   * @param productId ID of the product these files belong to (nullable)
   * @param userId ID of the authenticated user (nullable)
   * @return response DTOs with each file's metadata, in the same order as {@code files}
   * @throws BadRequestException if the list is empty, or any file is empty, has a disallowed type,
   *     or exceeds the size limit
   */
  @Override
  @Transactional
  public List<FileUploadResponse> storeAll(List<MultipartFile> files, Long productId, Long userId) {
    if (files == null || files.isEmpty()) {
      throw new BadRequestException("É necessário informar ao menos um arquivo.");
    }
    files.forEach(this::validateFile);

    return files.stream().map(file -> store(file, productId, userId)).collect(Collectors.toList());
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
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado. id=" + id));

    try {
      Path filePath = Paths.get(upload.getPath());
      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new ResourceNotFoundException("Arquivo não pode ser lido. id=" + id);
      }
      return resource;
    } catch (IOException e) {
      throw new RuntimeException("Falha ao carregar o arquivo. id=" + id, e);
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("O arquivo não pode estar vazio.");
    }
    if (file.getSize() > maxSizeBytes) {
      throw new BadRequestException(
          "O arquivo excede o tamanho máximo permitido de %d bytes.".formatted(maxSizeBytes));
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BadRequestException(
          "Tipo de arquivo não permitido: '%s'. Permitidos: %s."
              .formatted(contentType, ALLOWED_TYPES));
    }
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.'));
  }
}
