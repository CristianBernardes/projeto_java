package br.com.projetojava.upload.domain;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.product.domain.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing an uploaded file record.
 *
 * <p>Morphological table: each row belongs to one {@link Product} (many images per product) and
 * was uploaded by one {@link User}. The actual file content is stored on disk; this entity tracks
 * the path and associated metadata.
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

  /** Product this file belongs to. A product can have many files/images. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  /** User who uploaded this file. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User uploadedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Creates a new file upload record linked to a product and a user.
   *
   * @param originalName original file name as provided by the client
   * @param storedName UUID-based name used for storage (collision-safe)
   * @param contentType MIME type of the file
   * @param sizeBytes file size in bytes
   * @param path absolute path where the file is stored on disk
   * @param product product this file belongs to
   * @param uploadedBy user who performed the upload
   * @return new {@link FileUpload} instance
   */
  public static FileUpload create(
      String originalName,
      String storedName,
      String contentType,
      long sizeBytes,
      String path,
      Product product,
      User uploadedBy) {
    FileUpload upload = new FileUpload();
    upload.originalName = originalName;
    upload.storedName = storedName;
    upload.contentType = contentType;
    upload.sizeBytes = sizeBytes;
    upload.path = path;
    upload.product = product;
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
