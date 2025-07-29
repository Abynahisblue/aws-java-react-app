package com.webapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webapp.model.ImageMetadata;
import com.webapp.repository.ImageMetadataRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*") // Allow all origins for simplicity
public class S3Controller {

    private final S3Client s3Client;
    private final ImageMetadataRepository imageMetadataRepository;
    
    @Value("${s3.bucket.name}")
    private String bucketName;

    public S3Controller(S3Client s3Client, ImageMetadataRepository imageMetadataRepository) {
        this.s3Client = s3Client;
        this.imageMetadataRepository = imageMetadataRepository;
    }

    @GetMapping
    public ResponseEntity<?> listImages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<ImageMetadata> images = imageMetadataRepository.findAll(pageable);
        
        // Extract just the S3 URLs for the frontend
        List<String> imageUrls = images.getContent().stream()
            .map(ImageMetadata::getS3Url)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(imageUrls);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("description") String description) {
        try {
            // Upload to S3
            String fileName = file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            // Save metadata to database
            String s3Url = s3Client.utilities().getUrl(
                GetUrlRequest.builder().bucket(bucketName).key(fileName).build()
            ).toExternalForm();
            
            ImageMetadata metadata = new ImageMetadata();
            metadata.setFileName(fileName);
            metadata.setS3Url(s3Url);
            metadata.setDescription(description);
            metadata.setUploadedAt(LocalDateTime.now());
            
            imageMetadataRepository.save(metadata);
            
            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }
}
