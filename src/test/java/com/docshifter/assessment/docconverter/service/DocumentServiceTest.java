package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DocumentService documentService;

    private final Path uploadDir = Paths.get("uploads");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(uploadDir);
    }


    @Test
    void testUploadDocument() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
        when(multipartFile.getBytes()).thenReturn("test content".getBytes());

        Document savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setOriginalName("testFile.txt");
        savedDocument.setStatus("Uploaded");
        savedDocument.setUploadedAt(LocalDateTime.now());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        Document document = documentService.uploadDocument(multipartFile);

        assertNotNull(document);
        assertEquals("testFile.txt", document.getOriginalName());
        assertEquals("Uploaded", document.getStatus());
        verify(documentRepository, times(1)).save(any(Document.class));
        Path filePath = uploadDir.resolve("testFile.txt");
        Files.deleteIfExists(filePath);
    }

    @Test
    void testGetAllUploadedFileNames() throws IOException {
        Document document = new Document();
        document.setOriginalName("testFile.txt");

        when(documentRepository.findAll()).thenReturn(Collections.singletonList(document));

        List<String> fileNames = documentService.getAllUploadedFileNames();

        assertNotNull(fileNames);
        assertEquals(1, fileNames.size());
        assertEquals("testFile.txt", fileNames.getFirst());
        verify(documentRepository, times(1)).findAll();
        Path filePath = uploadDir.resolve("testFile.txt");
        Files.deleteIfExists(filePath);
    }


    @Test
    void testDeleteFileNotFound() throws IOException {
        boolean result = documentService.deleteFile("nonexistentFile.txt");

        assertFalse(result);
        verify(documentRepository, never()).deleteByOriginalName("nonexistentFile.txt");
    }

    @Test
    void testDeleteAllFiles() throws IOException {
        Document document = new Document();
        document.setOriginalName("testFile.txt");

        when(documentRepository.findAll()).thenReturn(Collections.singletonList(document));

        Path filePath = uploadDir.resolve("testFile.txt");
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);

        documentService.deleteAllFiles();

        assertFalse(Files.exists(filePath));
        verify(documentRepository, times(1)).findAll();
        verify(documentRepository, times(1)).deleteAll();
        Files.deleteIfExists(filePath);

    }

    @Test
    void testGetDocumentById() {
        Document document = new Document();
        document.setId(1L);
        document.setOriginalName("testFile.txt");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        Document foundDocument = documentService.getDocumentById(1L);

        assertNotNull(foundDocument);
        assertEquals("testFile.txt", foundDocument.getOriginalName());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDocumentByIdNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        Document foundDocument = documentService.getDocumentById(1L);

        assertNull(foundDocument);
        verify(documentRepository, times(1)).findById(1L);
    }
}
