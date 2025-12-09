package com.example.examenparcial3;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private static FirebaseFirestore db;

    // Inicializar Firestore
    public static void initializeFirestore() {
        try {
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firestore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FirebaseFirestore getFirestore() {
        if (db == null) {
            initializeFirestore();
        }
        return db;
    }

    public static CollectionReference getCollection(String collectionName) {
        if (getFirestore() != null) {
            return db.collection(collectionName);
        }
        return null;
    }

    public static DocumentReference getDocument(String collectionName, String documentId) {
        if (getFirestore() != null) {
            return db.collection(collectionName).document(documentId);
        }
        return null;
    }

    public static boolean isInitialized() {
        return db != null;
    }

    // Callback para guardar
    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Guarda o actualiza una entrevista (usa documentId si lo pasas, o crea automático si docId == null)
    public static void saveEntrevista(String documentId, Map<String, Object> entrevistaData, final SaveCallback callback) {
        try {
            CollectionReference coll = getCollection("entrevistas");
            if (coll == null) {
                if (callback != null) callback.onFailure(new Exception("Firestore no inicializado"));
                return;
            }

            if (documentId != null && !documentId.isEmpty()) {
                coll.document(documentId)
                        .set(entrevistaData)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e);
                        });
            } else {
                coll.add(entrevistaData)
                        .addOnSuccessListener(documentReference -> {
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e);
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saveEntrevista: " + e.getMessage());
            if (callback != null) callback.onFailure(e);
        }
    }
}
