package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

public class HybridStorageHelper {

    private static final String TAG = "HybridStorageHelper";

    // Guardar entrevista usando estrategia híbrida
    public static void saveEntrevista(Context context, Bitmap imagen, String audioSourcePath,
                                      String descripcion, String periodista, String fecha) {

        // Generar id de entrevista (puedes usar idOrden si lo tienes)
        String entrevistaId = UUID.randomUUID().toString();

        // Generar nombres únicos sin extensión (LocalStorageHelper añade extensión)
        String imageFileName = LocalStorageHelper.generateUniqueFileName("img");
        String audioFileName = LocalStorageHelper.generateUniqueFileName("audio");

        // Guardar localmente primero
        String localImagePath = LocalStorageHelper.saveImageLocally(context, imagen, imageFileName);
        String localAudioPath = LocalStorageHelper.saveAudioLocally(context, audioSourcePath, audioFileName);

        if (localImagePath == null || localAudioPath == null) {
            Toast.makeText(context, "Error al guardar archivos localmente", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear entrada parcial local (opcional: guardar en SQLite/SharedPreferences)
        Map<String, Object> entrevistaLocal = new HashMap<>();
        entrevistaLocal.put("id", entrevistaId);
        entrevistaLocal.put("imagenPathLocal", localImagePath);
        entrevistaLocal.put("audioPathLocal", localAudioPath);
        entrevistaLocal.put("descripcion", descripcion);
        entrevistaLocal.put("periodista", periodista);
        entrevistaLocal.put("fecha", fecha);
        entrevistaLocal.put("timestamp", new Date().getTime());
        entrevistaLocal.put("syncStatus", "syncing"); // estado inicial

        // Inicializar Firebase si es necesario
        if (!FirebaseStorageHelper.isInitialized()) {
            FirebaseStorageHelper.initializeStorage();
        }
        if (!FirestoreHelper.isInitialized()) {
            FirestoreHelper.initializeFirestore();
        }

        // Subir imagen primero
        File imageFile = new File(localImagePath);
        Uri imageUri = Uri.fromFile(imageFile);
        String remoteImagePath = "entrevistas/" + entrevistaId + "/imagen.jpg"; // ruta en Storage

        FirebaseStorageHelper.uploadFile(imageUri, remoteImagePath, new FirebaseStorageHelper.UrlCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Imagen subida. URL: " + imageUrl);

                // Subir audio
                File audioFile = new File(localAudioPath);
                Uri audioUri = Uri.fromFile(audioFile);
                String remoteAudioPath = "entrevistas/" + entrevistaId + "/audio.3gp";

                FirebaseStorageHelper.uploadFile(audioUri, remoteAudioPath, new FirebaseStorageHelper.UrlCallback() {
                    @Override
                    public void onSuccess(String audioUrl) {
                        Log.d(TAG, "Audio subido. URL: " + audioUrl);

                        // Guardar metadata final en Firestore
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("id", entrevistaId);
                        doc.put("descripcion", descripcion);
                        doc.put("periodista", periodista);
                        doc.put("fecha", fecha);
                        doc.put("imagenUrl", imageUrl);
                        doc.put("audioUrl", audioUrl);
                        doc.put("timestamp", new Date().getTime());

                        FirestoreHelper.saveEntrevista(entrevistaId, doc, new FirestoreHelper.SaveCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Entrevista guardada en Firestore con id: " + entrevistaId);
                                // Aquí podrías actualizar estado local a "synced"
                                Toast.makeText(context, "Entrevista sincronizada con éxito", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Error guardando en Firestore: " + e.getMessage());
                                Toast.makeText(context, "Error guardando metadata en Firestore", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error subiendo audio: " + e.getMessage());
                        Toast.makeText(context, "Error subiendo audio a Firebase", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error subiendo imagen: " + e.getMessage());
                Toast.makeText(context, "Error subiendo imagen a Firebase", Toast.LENGTH_LONG).show();
            }
        });

        // Nota: puedes guardar 'entrevistaLocal' en SQLite/SharedPreferences si quieres manejar reintentos offline.
    }

    // Métodos de reintento y verificación de estado pueden implementarse usando Firestore o una DB local
    public static void retryFailedSyncs(Context context) {
        Log.d(TAG, "Reintentando sincronizaciones fallidas...");
        // Implementar escaneo de DB local y reintentos
    }

    public static String getSyncStatus(String entrevistaId) {
        // Extraer desde DB local o Firestore si quieres
        return "unknown";
    }
}
