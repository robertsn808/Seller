package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.PropertyPhoto;
import com.realestate.sellerfunnel.model.Seller;
import com.realestate.sellerfunnel.repository.PropertyPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    private PropertyPhotoRepository photoRepository;

    public List<PropertyPhoto> savePropertyPhotos(List<MultipartFile> files, Seller seller) throws IOException {
        List<PropertyPhoto> savedPhotos = new ArrayList<>();
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        int displayOrder = 1;
        for (MultipartFile file : files) {
            if (!file.isEmpty() && isValidImageFile(file)) {
                PropertyPhoto photo = savePhoto(file, seller, displayOrder++);
                savedPhotos.add(photo);
            }
        }
        
        return savedPhotos;
    }

    private PropertyPhoto savePhoto(MultipartFile file, Seller seller, int displayOrder) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalName);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        PropertyPhoto photo = new PropertyPhoto(
            fileName,
            originalName,
            filePath.toString(),
            file.getSize(),
            file.getContentType()
        );
        photo.setSeller(seller);
        photo.setDisplayOrder(displayOrder);
        
        return photoRepository.save(photo);
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "jpg";
    }

    public void deletePhoto(PropertyPhoto photo) throws IOException {
        // Delete from filesystem
        Path filePath = Paths.get(photo.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Delete from database
        photoRepository.delete(photo);
    }

    public byte[] getPhotoData(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        return null;
    }
}