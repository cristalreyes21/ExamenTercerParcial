package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalStorageHelper {

    private static final String TAG = "LocalStorageHelper";
    private static final String IMAGES_FOLDER = "entrevistas_images";
    private static final String AUDIO_FOLDER = "entrevistas_audio";

    // Guardar imagen localmente (fileName sin extension — se añade .jpg aquí)
    public static String saveImageLocally(Context context, Bitmap bitmap, String fileNameWithoutExt) {
        try {
            File imagesDir = new File(context.getFilesDir(), IMAGES_FOLDER);
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            String fileName = fileNameWithoutExt;
            if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
                fileName = fileName + ".jpg";
            }

            File imageFile = new File(imagesDir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            Log.d(TAG, "Imagen guardada localmente: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error al guardar imagen localmente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Guardar audio localmente copiando por streams (sourcePath es ruta absoluta origen)
    // fileNameWithoutExt no incluye extension; se le añade .3gp si no tiene extension
    public static String saveAudioLocally(Context context, String sourcePath, String fileNameWithoutExt) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File audioDir = new File(context.getFilesDir(), AUDIO_FOLDER);
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }

            String fileName = fileNameWithoutExt;
            if (!fileName.toLowerCase().endsWith(".3gp") && !fileName.toLowerCase().endsWith(".mp4") && !fileName.toLowerCase().contains(".")) {
                fileName = fileName + ".3gp";
            }

            File destFile = new File(audioDir, fileName);

            File srcFile = new File(sourcePath);
            if (!srcFile.exists()) {
                Log.e(TAG, "Archivo origen no existe: " + sourcePath);
                return null;
            }

            in = new java.io.FileInputStream(srcFile);
            out = new java.io.FileOutputStream(destFile);

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();

            Log.d(TAG, "Audio guardado localmente: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error al guardar audio localmente: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {}
            try {
                if (out != null) out.close();
            } catch (IOException ignored) {}
        }
    }

    public static Bitmap loadImageFromLocal(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imagen local: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String generateUniqueFileName(String prefix) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timestamp + "_" + System.currentTimeMillis();
    }

    public static long getImagesFolderSize(Context context) {
        File imagesDir = new File(context.getFilesDir(), IMAGES_FOLDER);
        return getFolderSize(imagesDir);
    }

    public static long getAudioFolderSize(Context context) {
        File audioDir = new File(context.getFilesDir(), AUDIO_FOLDER);
        return getFolderSize(audioDir);
    }

    private static long getFolderSize(File directory) {
        long size = 0;
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    public static void cleanupOldFiles(Context context, int maxAgeInDays) {
        long cutoffTime = System.currentTimeMillis() - (maxAgeInDays * 24L * 60L * 60L * 1000L);
        cleanupFolder(new File(context.getFilesDir(), IMAGES_FOLDER), cutoffTime);
        cleanupFolder(new File(context.getFilesDir(), AUDIO_FOLDER), cutoffTime);
    }

    private static void cleanupFolder(File folder, long cutoffTime) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            Log.d(TAG, "Archivo antiguo eliminado: " + file.getName());
                        }
                    }
                }
            }
        }
    }
}
