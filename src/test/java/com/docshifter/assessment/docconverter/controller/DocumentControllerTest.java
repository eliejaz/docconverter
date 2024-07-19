package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.service.implementation.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DocumentController documentController;

    private Document document;
    private final Path uploadDir = Paths.get("uploads");

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(uploadDir);
        document = new Document();
        document.setId(1L);
        document.setOriginalName("testFile.txt");
        document.setFilePath(uploadDir.resolve("testFile.txt").toString());
    }

    @Test
    void testUploadDocument() throws Exception {
        when(documentService.uploadDocument(any(MultipartFile.class))).thenReturn(document);

        ResponseEntity<Document> response = documentController.uploadDocument(multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(document, response.getBody());
        verify(documentService, times(1)).uploadDocument(multipartFile);
    }

    @Test
    void testUploadDocumentError() throws Exception {
        when(documentService.uploadDocument(any(MultipartFile.class))).thenThrow(new IOException("Upload error"));

        ResponseEntity<Document> response = documentController.uploadDocument(multipartFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService, times(1)).uploadDocument(multipartFile);
    }

    @Test
    void testGetAllFiles() {
        when(documentService.getAllFiles()).thenReturn(Collections.singletonList(document));

        ResponseEntity<List<Document>> response = documentController.getAllFiles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(documentService, times(1)).getAllFiles();
    }

    @Test
    void testGetAllUploadedFileNames() {
        when(documentService.getAllUploadedFileNames()).thenReturn(Collections.singletonList("testFile.txt"));

        ResponseEntity<List<String>> response = documentController.getAllUploadedFileNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("testFile.txt", response.getBody().getFirst());
        verify(documentService, times(1)).getAllUploadedFileNames();
    }

    @Test
    void testDeleteFile() throws Exception {
        when(documentService.deleteFile("testFile.txt")).thenReturn(true);

        ResponseEntity<String> response = documentController.deleteFile("testFile.txt");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File deleted successfully.", response.getBody());
        verify(documentService, times(1)).deleteFile("testFile.txt");
    }

    @Test
    void testDeleteFileNotFound() throws Exception {
        when(documentService.deleteFile("testFile.txt")).thenReturn(false);

        ResponseEntity<String> response = documentController.deleteFile("testFile.txt");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("File not found.", response.getBody());
        verify(documentService, times(1)).deleteFile("testFile.txt");
    }

    @Test
    void testDeleteFileError() throws Exception {
        when(documentService.deleteFile("testFile.txt")).thenThrow(new IOException("Delete error"));

        ResponseEntity<String> response = documentController.deleteFile("testFile.txt");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("An error occurred:"));
        verify(documentService, times(1)).deleteFile("testFile.txt");
    }

    @Test
    void testDeleteAllFiles() throws Exception {
        doNothing().when(documentService).deleteAllFiles();

        ResponseEntity<String> response = documentController.deleteAllFiles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All files deleted successfully.", response.getBody());
        verify(documentService, times(1)).deleteAllFiles();
    }

    @Test
    void testDeleteAllFilesError() throws Exception {
        doThrow(new IOException("Delete all error")).when(documentService).deleteAllFiles();

        ResponseEntity<String> response = documentController.deleteAllFiles();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("An error occurred:"));
        verify(documentService, times(1)).deleteAllFiles();
    }


    @Test
    void testDownloadFileNotFound() {
        when(documentService.getDocumentByOriginalName("testFile.txt")).thenReturn(null);

        ResponseEntity<InputStreamResource> response = documentController.downloadFile("testFile.txt");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService, times(1)).getDocumentByOriginalName("testFile.txt");
    }


    @Test
    void testGetDocumentDetails() {
        when(documentService.getDocumentById(1L)).thenReturn(document);

        ResponseEntity<Document> response = documentController.getDocumentDetails(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(document, response.getBody());
        verify(documentService, times(1)).getDocumentById(1L);
    }

    @Test
    void testGetDocumentDetailsNotFound() {
        when(documentService.getDocumentById(1L)).thenReturn(null);

        ResponseEntity<Document> response = documentController.getDocumentDetails(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService, times(1)).getDocumentById(1L);
    }

    @Test
    void testGetDocumentDetailsError() {
        when(documentService.getDocumentById(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Document> response = documentController.getDocumentDetails(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService, times(1)).getDocumentById(1L);
    }
}
