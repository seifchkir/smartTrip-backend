package com.SmarTrip.smarTrip_backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FirebaseServiceTest {

    @Autowired
    private FirebaseService firebaseService;

    private String testCollection;
    private String testDocumentId;
    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        // Use a test collection with a timestamp to avoid conflicts
        testCollection = "test_" + System.currentTimeMillis();
        testDocumentId = UUID.randomUUID().toString();
        
        testData = new HashMap<>();
        testData.put("name", "Test User");
        testData.put("email", "test@example.com");
        testData.put("age", 25);
    }

    @Test
    void testSaveAndGetDocument() throws Exception {
        // Save document
        String updateTime = firebaseService.saveDocument(testCollection, testDocumentId, testData);
        assertNotNull(updateTime);
        
        // Get document
        Map<String, Object> retrievedData = firebaseService.getDocument(testCollection, testDocumentId);
        assertNotNull(retrievedData);
        assertEquals("Test User", retrievedData.get("name"));
        assertEquals("test@example.com", retrievedData.get("email"));
        assertEquals(25L, retrievedData.get("age"));
    }

    @Test
    void testGetAllDocuments() throws Exception {
        // Create multiple documents
        for (int i = 0; i < 3; i++) {
            String docId = "doc_" + i;
            Map<String, Object> data = new HashMap<>();
            data.put("index", i);
            firebaseService.saveDocument(testCollection, docId, data);
        }
        
        // Get all documents
        Map<String, Object> allDocs = firebaseService.getAllDocuments(testCollection);
        assertNotNull(allDocs);
        assertTrue(allDocs.size() >= 3);
    }

    @Test
    void testGetDocumentsWhere() throws Exception {
        // Create documents with different categories
        for (int i = 0; i < 5; i++) {
            String docId = "category_doc_" + i;
            Map<String, Object> data = new HashMap<>();
            data.put("category", i % 2 == 0 ? "even" : "odd");
            data.put("value", i);
            firebaseService.saveDocument(testCollection, docId, data);
        }
        
        // Get documents where category is "even"
        Map<String, Object> evenDocs = firebaseService.getDocumentsWhere(testCollection, "category", "even");
        assertNotNull(evenDocs);
        
        // Check that all retrieved documents have category "even"
        for (Object doc : evenDocs.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> docData = (Map<String, Object>) doc;
            assertEquals("even", docData.get("category"));
        }
    }

    @Test
    void testDeleteDocument() throws Exception {
        // Save document
        firebaseService.saveDocument(testCollection, testDocumentId, testData);
        
        // Verify document exists
        Map<String, Object> beforeDelete = firebaseService.getDocument(testCollection, testDocumentId);
        assertNotNull(beforeDelete);
        
        // Delete document
        String deleteTime = firebaseService.deleteDocument(testCollection, testDocumentId);
        assertNotNull(deleteTime);
        
        // Verify document no longer exists
        Map<String, Object> afterDelete = firebaseService.getDocument(testCollection, testDocumentId);
        assertNull(afterDelete);
    }
}