package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_photos")
public class PropertyPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "original_name")
    private String originalName;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
    
    // Constructors
    public PropertyPhoto() {}
    
    public PropertyPhoto(String fileName, String originalName, String filePath, 
                        Long fileSize, String contentType) {
        this.fileName = fileName;
        this.originalName = originalName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}