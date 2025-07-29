package com.webapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "image_metadata")
@Getter
@Setter
public class ImageMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String s3Url;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    // Constructors, getters, setters
}