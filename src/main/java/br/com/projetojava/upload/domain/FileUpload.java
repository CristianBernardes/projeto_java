package br.com.projetojava.upload.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing an uploaded file record.
 *
 * <p>Stores metadata about files uploaded to the server. The actual file content is stored on disk;
 * this entity tracks the path and associated metadata.
 */
@Entity
@Table(name = "file_uploads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUpload {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "original_name", nullable = false)
  private String originalName;

  @Column(name = "stored_name", nullable = false, unique = true)
  private String storedName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "uploaded_by")
  private Long uploadedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Creates a new file upload record.
   *
   * @param originalName original file name as provided by the client
   * @param storedName UUID-based name used for storage (collision-safe)
   * @param contentType MIME type of the file
   * @param sizeBytes file size in bytes
   * @param path absolute path where the file is stored on disk
   * @param uploadedBy ID of the user who uploaded the file (nullable)
   * @return new {@link FileUpload} instance
   */
  public static FileUpload create(
      String originalName,
      String storedName,
      String contentType,
      long sizeBytes,
      String path,
      Long uploadedBy) {
    FileUpload upload = new FileUpload();
    upload.originalName = originalName;
    upload.storedName = storedName;
    upload.contentType = contentType;
    upload.sizeBytes = sizeBytes;
    upload.path = path;
    upload.uploadedBy = uploadedBy;
    upload.createdAt = LocalDateTime.now();
    return upload;
  }

  /**
   * Two file uploads are equal if they share the same persisted {@code id}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FileUpload other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type.
   *
   * @return constant hash for all {@link FileUpload} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
