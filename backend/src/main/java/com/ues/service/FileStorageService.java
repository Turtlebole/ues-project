package com.ues.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LogManager.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file: {}", fileName);
            return fileName;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Could not store file " + fileName, e);
        }
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName);
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to delete file {}: {}", fileName, e.getMessage());
        }
    }
}
