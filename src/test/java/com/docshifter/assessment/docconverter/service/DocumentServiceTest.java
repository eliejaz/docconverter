package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.dto.StatusChangeNotification;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import com.docshifter.assessment.docconverter.service.implementation.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    @Mock
    private SimpMessagingTemplate template;

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
        savedDocument.setStatus(DocumentStatus.UPLOADED);
        savedDocument.setUploadedAt(LocalDateTime.now());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        Document document = documentService.uploadDocument(multipartFile);

        assertNotNull(document);
        assertEquals("testFile.txt", document.getOriginalName());
        assertEquals(DocumentStatus.UPLOADED, document.getStatus());
        verify(documentRepository, times(1)).save(any(Document.class));
        Path filePath = uploadDir.resolve("testFile.txt");
        Files.deleteIfExists(filePath);
    }

    @Test
    void testUploadDocumentWithNullFile() {
        assertThrows(NullPointerException.class, () -> {
            documentService.uploadDocument(null);
        });
    }

    @Test
    void testGetAllUploadedFileNames() throws IOException {
        Document document = new Document();
        document.setOriginalName("testFile.txt");
        document.setStatus(DocumentStatus.UPLOADED);
        when(documentRepository.findAll()).thenReturn(Collections.singletonList(document));

        List<String> fileNames = documentService.getAllUploadedFileNames();

        assertNotNull(fileNames);
        assertEquals(1, fileNames.size());
        assertEquals("testFile.txt", fileNames.get(0));
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUploadedFileNamesEmpty() {
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        List<String> fileNames = documentService.getAllUploadedFileNames();

        assertNotNull(fileNames);
        assertTrue(fileNames.isEmpty());
        verify(documentRepository, times(1)).findAll();
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
        Long documentId = 1L;
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.getDocumentById(documentId);
        });

        assertEquals("Document with id1 not found", exception.getMessage());
        verify(documentRepository, times(1)).findById(documentId);
    }

    @Test
    void testCreateRequestedDocument() {
        Document originalDocument = new Document();
        originalDocument.setId(1L);
        originalDocument.setOriginalName("testFile.txt");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(originalDocument));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        String conversionId = documentService.createRequestedDocument(1L);

        assertNotNull(conversionId);
        assertNotEquals("", conversionId);
        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testUpdateDocumentStatus() {
        Document document = new Document();
        document.setConversionId("1234");
        document.setStatus(DocumentStatus.REQUESTED);

        when(documentRepository.findByConversionId("1234")).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        documentService.updateDocumentStatus("1234", DocumentStatus.COMPLETED);

        assertEquals(DocumentStatus.COMPLETED, document.getStatus());
        verify(documentRepository, times(1)).findByConversionId("1234");
        verify(documentRepository, times(1)).save(document);
        verify(template, times(1)).convertAndSend(eq("/topic/notification"), any(StatusChangeNotification.class));

    }

    @Test
    void testUpdateDocumentStatusWithConvertedFilePath() {
        Document document = new Document();
        document.setConversionId("1234");
        document.setStatus(DocumentStatus.REQUESTED);

        when(documentRepository.findByConversionId("1234")).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        documentService.updateDocumentStatus("1234", DocumentStatus.COMPLETED, "convertedFile.txt", LocalDateTime.now(), "converted/convertedFile.txt");

        assertEquals(DocumentStatus.COMPLETED, document.getStatus());
        assertEquals("convertedFile.txt", document.getConvertedName());
        assertNotNull(document.getConvertedAt());
        assertEquals("converted/convertedFile.txt", document.getConvertedFilePath());
        verify(documentRepository, times(1)).findByConversionId("1234");
        verify(documentRepository, times(1)).save(document);
    }

    @Test
    void testGetConversionStatus() {
        Document document = new Document();
        document.setConversionId("1234");
        document.setStatus(DocumentStatus.REQUESTED);

        when(documentRepository.findByConversionId("1234")).thenReturn(Optional.of(document));

        DocumentStatus status = documentService.getConversionStatus("1234");

        assertEquals(DocumentStatus.REQUESTED, status);
        verify(documentRepository, times(1)).findByConversionId("1234");
    }

    @Test
    void testGetDocumentWithConversionIDCompleted() {
        Document document = new Document();
        document.setConversionId("1234");
        document.setStatus(DocumentStatus.COMPLETED);

        when(documentRepository.findByConversionId("1234")).thenReturn(Optional.of(document));

        Optional<Document> foundDocument = documentService.getCompletedDocumentByConversionID("1234");

        assertTrue(foundDocument.isPresent());
        assertEquals(document, foundDocument.get());
        verify(documentRepository, times(1)).findByConversionId("1234");
    }

    @Test
    void testGetDocumentWithConversionIDNotCompleted() {
        Document document = new Document();
        document.setConversionId("1234");
        document.setStatus(DocumentStatus.REQUESTED);

        when(documentRepository.findByConversionId("1234")).thenReturn(Optional.of(document));

        Optional<Document> foundDocument = documentService.getCompletedDocumentByConversionID("1234");

        assertFalse(foundDocument.isPresent());
        verify(documentRepository, times(1)).findByConversionId("1234");
    }
}
