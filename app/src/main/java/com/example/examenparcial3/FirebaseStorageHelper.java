package com.example.examenparcial3;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseStorageHelper {

    private static final String TAG = "FirebaseStorageHelper";
    private static FirebaseStorage storage;
    private static StorageReference storageRef;

    public interface UrlCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    // Inicializar Firebase Storage
    public static void initializeStorage() {
        try {
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            Log.d(TAG, "Firebase Storage inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase Storage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FirebaseStorage getStorage() {
        if (storage == null) {
            initializeStorage();
        }
        return storage;
    }

    public static StorageReference getStorageReference() {
        if (storageRef == null) {
            initializeStorage();
        }
        return storageRef;
    }

    public static StorageReference getFileReference(String path) {
        if (getStorageReference() != null) {
            return storageRef.child(path);
        }
        return null;
    }

    public static StorageReference getFolderReference(String folderPath) {
        if (getStorageReference() != null) {
            return storageRef.child(folderPath);
        }
        return null;
    }

    // Metodo genérico para subir un archivo desde un Uri y devolver el downloadUrl via callback
    public static void uploadFile(Uri fileUri, String remotePath, final UrlCallback callback) {
        try {
            StorageReference ref = getFileReference(remotePath);
            if (ref == null) {
                if (callback != null) callback.onFailure(new Exception("storageRef es null"));
                return;
            }
            UploadTask uploadTask = ref.putFile(fileUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // obtener la url de descarga
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (callback != null) callback.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en uploadFile: " + e.getMessage());
            if (callback != null) callback.onFailure(e);
        }
    }

    public static boolean isInitialized() {
        return storage != null && storageRef != null;
    }
}
