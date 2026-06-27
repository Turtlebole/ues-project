package com.ues.controller;

import com.ues.service.MinioStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * Streams location images and PDF documents stored in MinIO.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioStorageService minioStorageService;

    public FileController(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    @GetMapping("/{objectName}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String objectName) {
        InputStream stream = minioStorageService.getFile(objectName);
        String contentType = minioStorageService.getContentType(objectName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + objectName + "\"")
                .body(new InputStreamResource(stream));
    }
}
