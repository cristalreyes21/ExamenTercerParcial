package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Base64StorageHelper {
    
    private static final String TAG = "Base64StorageHelper";
    
    // Guardar entrevista con imagen en Base64
    public static void saveEntrevistaWithBase64(Context context, Bitmap imagen, String audioPath, 
                                               String descripcion, String periodista, String fecha) {
        try {
            Toast.makeText(context, "Guardando entrevista...", Toast.LENGTH_SHORT).show();
            
            // Paso 1: Convertir imagen a Base64
            String imagenBase64 = convertImageToBase64(imagen);
            if (imagenBase64 == null) {
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Paso 2: Guardar audio localmente
            String localAudioPath = LocalStorageHelper.saveAudioLocally(context, audioPath, "audio_" + System.currentTimeMillis());
            if (localAudioPath == null) {
                Toast.makeText(context, "Error al guardar el audio", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Paso 3: Crear objeto de entrevista
            String entrevistaId = UUID.randomUUID().toString();
            Map<String, Object> entrevista = new HashMap<>();
            entrevista.put("id", entrevistaId);
            entrevista.put("imagenBase64", imagenBase64);
            entrevista.put("audioPath", localAudioPath);
            entrevista.put("descripcion", descripcion);
            entrevista.put("periodista", periodista);
            entrevista.put("fecha", fecha);
            entrevista.put("timestamp", System.currentTimeMillis());
            entrevista.put("imagenSize", imagenBase64.length());
            entrevista.put("storageType", "base64");
            
            // Paso 4: Guardar en Firebase Database
            saveToFirebaseDatabase(entrevista, entrevistaId, context);
            
        } catch (Exception e) {
            Toast.makeText(context, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error al guardar entrevista: " + e.getMessage(), e);
        }
    }
    
    // Convertir imagen a Base64
    private static String convertImageToBase64(Bitmap imagen) {
        try {
            // Comprimir imagen para reducir tamaño
            Bitmap imagenComprimida = comprimirImagen(imagen);
            
            // Convertir a bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imagenComprimida.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Calidad 70%
            byte[] imageBytes = baos.toByteArray();
            
            // Convertir a Base64
            String base64String = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            
            Log.d(TAG, "Imagen convertida a Base64. Tamaño: " + base64String.length() + " caracteres");
            return base64String;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir imagen a Base64: " + e.getMessage(), e);
            return null;
        }
    }
    
    // Comprimir imagen para reducir tamaño
    private static Bitmap comprimirImagen(Bitmap imagenOriginal) {
        if (imagenOriginal == null) return null;
        
        // Redimensionar si es muy grande
        int maxWidth = 800;  // Reducido para Base64
        int maxHeight = 800;
        
        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return imagenOriginal;
        }
        
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(imagenOriginal, newWidth, newHeight, true);
    }
    
    // Guardar en Firebase Database
    private static void saveToFirebaseDatabase(Map<String, Object> entrevista, String entrevistaId, Context context) {
        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");
            
            dbRef.child(entrevistaId).setValue(entrevista)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Entrevista guardada exitosamente en Firebase Database");
                    Toast.makeText(context, "¡Entrevista guardada exitosamente!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar en Firebase Database: " + e.getMessage(), e);
                    Toast.makeText(context, "Error al guardar en Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error en Firebase Database: " + e.getMessage(), e);
            Toast.makeText(context, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    // Cargar imagen desde Base64
    public static Bitmap loadImageFromBase64(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return null;
            }
            
            // Decodificar Base64 a bytes
            byte[] imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            
            // Convertir bytes a Bitmap
            Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            
            Log.d(TAG, "Imagen cargada desde Base64. Tamaño: " + imageBytes.length + " bytes");
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imagen desde Base64: " + e.getMessage(), e);
            return null;
        }
    }
    
    // Verificar tamaño de Base64
    public static boolean isBase64SizeReasonable(String base64String) {
        if (base64String == null) return false;
        
        // Firebase Database tiene un límite de 1MB por nodo
        // Base64 es aproximadamente 33% más grande que el archivo original
        int maxSize = 750000; // ~750KB en Base64 = ~1MB en Firebase
        
        return base64String.length() <= maxSize;
    }
    
    // Obtener información de la imagen
    public static Map<String, Object> getImageInfo(String base64String) {
        Map<String, Object> info = new HashMap<>();
        
        if (base64String != null) {
            info.put("base64Length", base64String.length());
            info.put("estimatedSizeKB", Math.round(base64String.length() * 0.75 / 1024.0));
            info.put("isReasonableSize", isBase64SizeReasonable(base64String));
        }
        
        return info;
    }
}
