package com.SmarTrip.smarTrip_backend.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    // Get Firestore database instance
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
    
    // Create or update a document
    public String saveDocument(String collection, String documentId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        ApiFuture<WriteResult> result = firestore.collection(collection).document(documentId).set(data);
        
        // Return the update time
        return result.get().getUpdateTime().toString();
    }
    
    // Read a document
    public Map<String, Object> getDocument(String collection, String documentId) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        DocumentReference docRef = firestore.collection(collection).document(documentId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.getData();
        } else {
            return null;
        }
    }
    
    // Get all documents in a collection
    public Map<String, Object> getAllDocuments(String collection) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(collection).get();
        
        Map<String, Object> documents = new HashMap<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            documents.put(document.getId(), document.getData());
        }
        
        return documents;
    }
    
    // Get documents where field equals value
    public Map<String, Object> getDocumentsWhere(String collection, String field, String value) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(collection).whereEqualTo(field, value).get();
        
        Map<String, Object> documents = new HashMap<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            documents.put(document.getId(), document.getData());
        }
        
        return documents;
    }
    
    // Get documents where multiple fields equal values
    public Map<String, Object> getDocumentsWhere(String collection, String field1, String value1, String field2, String value2) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection(collection)
                .whereEqualTo(field1, value1)
                .whereEqualTo(field2, value2)
                .get();
        
        Map<String, Object> documents = new HashMap<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            documents.put(document.getId(), document.getData());
        }
        
        return documents;
    }
    
    // Delete a document
    public String deleteDocument(String collection, String documentId) throws ExecutionException, InterruptedException {
        Firestore firestore = getFirestore();
        ApiFuture<WriteResult> result = firestore.collection(collection).document(documentId).delete();
        
        // Return the update time
        return result.get().getUpdateTime().toString();
    }
}