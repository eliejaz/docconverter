package com.docshifter.assessment.docconverter.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String convertedName;
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
    private String filePath;
    private String convertedFilePath;
    private LocalDateTime uploadedAt;
    private LocalDateTime convertedAt;
    private String conversionId;
}
