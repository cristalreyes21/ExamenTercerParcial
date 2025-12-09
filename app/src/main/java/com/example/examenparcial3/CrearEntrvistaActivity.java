package com.example.examenparcial3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrearEntrvistaActivity extends AppCompatActivity {

    static final int peticion_acceso_camara = 101;
    static final int peticion_captura_imagen = 102;
    ImageView ObjectoImagen;
    Button btncaptura, btnenviar;
    Button recordButton, stopButton, playButton;
    EditText descripciontxt, periodistatxt;
    TextView fecha;
    private MediaRecorder recorder;
    private MediaPlayer player;
    String fileName;
    Bitmap imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crear_entrvista);
        FirebaseStorageHelper.initializeStorage(); // inicializa Storage

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ObjectoImagen = findViewById(R.id.imageView);
        btncaptura = findViewById(R.id.btntakefoto);
        fecha = findViewById(R.id.textViewFecha);

        btncaptura.setOnClickListener(v -> {
            hideKeyboard();
            Permisos();
        });

        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);

        fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        descripciontxt = findViewById(R.id.editTextdescription);
        periodistatxt = findViewById(R.id.editTextPeriodista);
        showCurrentDate(this);

        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) mainLayout.setOnClickListener(v -> hideKeyboard());

        if (descripciontxt != null) {
            descripciontxt.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (periodistatxt != null) periodistatxt.requestFocus();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        if (periodistatxt != null) {
            periodistatxt.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        recordButton.setOnClickListener(v -> {
            hideKeyboard();
            startRecording();
            recordButton.setEnabled(false);
            stopButton.setEnabled(true);
            playButton.setEnabled(false);
        });

        stopButton.setOnClickListener(v -> {
            hideKeyboard();
            stopRecording();
            recordButton.setEnabled(true);
            stopButton.setEnabled(false);
            playButton.setEnabled(true);
        });

        playButton.setOnClickListener(v -> {
            hideKeyboard();
            startPlaying();
        });

        Button btnTestFirebase = findViewById(R.id.btnTestFirebase);
        btnTestFirebase.setOnClickListener(v -> {
            hideKeyboard();
            probarConectividadFirebase();
        });

        btnenviar = findViewById(R.id.btnupload);
        btnenviar.setOnClickListener(v -> {
            hideKeyboard();
            String descrip = descripciontxt.getText().toString();
            String perio = periodistatxt.getText().toString();
            if (fileName.length() == 0 || imagen == null || descripciontxt.getText().length() == 0 || periodistatxt.getText().length() == 0) {
                Toast.makeText(CrearEntrvistaActivity.this, "Llene todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                uploadDataToFirebase(imagen, fileName, descrip, perio, fecha.getText().toString());
            }
        });
    }

    // guardar entrevista en base64 (corrige conversión)
    private void saveEntrevistaWithBase64(Bitmap imagen, String audioPath, String descripcion, String periodista, String fecha) {
        try {

            Base64StorageHelper.saveEntrevistaWithBase64(
                    this,
                    imagen,
                    audioPath,
                    descripcion,
                    periodista,
                    fecha
            );

            limpiarCampos();
            new android.os.Handler().postDelayed(this::finish, 1500);

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Base64Storage", "Error al guardar entrevista: " + e.getMessage(), e);
        }
    }



    // fotografia codigo
    private void Permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    peticion_acceso_camara);
        } else {
            TomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == peticion_acceso_camara) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "Acceso Denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, peticion_captura_imagen);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == peticion_captura_imagen && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                imagen = (Bitmap) extras.get("data");
                ObjectoImagen.setImageBitmap(imagen);
            }
        }
    }

    // audio
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);
        try {
            recorder.prepare();
            recorder.start();
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try { recorder.stop(); } catch (RuntimeException ignored) {}
            recorder.release();
            recorder = null;
            Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
            Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
        if (player != null) { player.release(); player = null; }
        if (recorder != null) { recorder.release(); recorder = null; }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    public void showCurrentDate(Context context) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        fecha.setText(currentDate);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private Bitmap comprimirImagen(Bitmap imagenOriginal) {
        if (imagenOriginal == null) return null;
        int maxWidth = 1024;
        int maxHeight = 1024;
        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        if (width <= maxWidth && height <= maxHeight) return imagenOriginal;
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(imagenOriginal, newWidth, newHeight, true);
    }

    private void probarConectividadFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("test");
        if (databaseRef != null) {
            databaseRef.child("conexion").setValue("test")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Database conectado exitosamente", Toast.LENGTH_SHORT).show();
                        databaseRef.child("conexion").removeValue();
                        probarFirebaseStorage();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error de conexión a Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
        } else {
            Toast.makeText(this, "Error: Firebase Database no está inicializado", Toast.LENGTH_LONG).show();
        }
    }

    private void probarFirebaseStorage() {
        try {
            Toast.makeText(this, "Probando Firebase Storage...", Toast.LENGTH_SHORT).show();
            com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String testPath = "test_conexion.txt";
            StorageReference testRef = storageRef.child(testPath);
            byte[] testData = "prueba_conexion".getBytes();
            testRef.putBytes(testData)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(this, "¡Storage funciona correctamente!", Toast.LENGTH_LONG).show();
                        testRef.delete().addOnSuccessListener(aVoid -> Log.d("StorageTest", "Archivo de prueba eliminado correctamente"));
                    })
                    .addOnFailureListener(e -> {
                        String errorMsg = "Error de Storage: " + e.getMessage();
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("StorageTest", errorMsg, e);
                    });
        } catch (Exception e) {
            String errorMsg = "Excepción al probar Storage: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e("StorageTest", errorMsg, e);
        }
    }

    // Convertir bitmap a Base64
    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void uploadDataToFirebase(Bitmap imagenBitmap, String audioPath, String descripcion, String periodista, String fecha) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_LONG).show();
            return;
        }
        if (imagenBitmap == null) {
            Toast.makeText(this, "Error: La imagen es nula", Toast.LENGTH_LONG).show();
            return;
        }
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(this, "Error: La ruta del audio es inválida", Toast.LENGTH_LONG).show();
            return;
        }
        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            Toast.makeText(this, "Error: El archivo de audio no existe", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Iniciando subida...", Toast.LENGTH_SHORT).show();
        Bitmap imagenComprimida = comprimirImagen(imagenBitmap);
        if (imagenComprimida == null) {
            Toast.makeText(this, "Error: No se pudo procesar la imagen", Toast.LENGTH_LONG).show();
            return;
        }
        if (!FirebaseStorageHelper.isInitialized()) FirebaseStorageHelper.initializeStorage();
        StorageReference storageRef = FirebaseStorageHelper.getStorageReference();
        if (storageRef == null) {
            Toast.makeText(this, "Error: Firebase Storage no está inicializado", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference databaseRef = MyApplication.getDatabaseReference("entrevistas");
        if (databaseRef == null) {
            Toast.makeText(this, "Error: Firebase no está inicializado", Toast.LENGTH_LONG).show();
            return;
        }

        String id = databaseRef.push().getKey();
        String imagenPath = "imagenes/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imagenRef = storageRef.child(imagenPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenComprimida.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] dataImagen = baos.toByteArray();

        imagenRef.putBytes(dataImagen)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                    imagenRef.getDownloadUrl().addOnSuccessListener(imagenUrl -> {
                        Toast.makeText(this, "URL de imagen obtenida", Toast.LENGTH_SHORT).show();
                        String audioPathInStorage = "audios/" + UUID.randomUUID().toString() + ".3gp";
                        StorageReference audioRef = storageRef.child(audioPathInStorage);
                        Uri file = Uri.fromFile(audioFile);
                        audioRef.putFile(file)
                                .addOnSuccessListener(taskSnapshot1 -> {
                                    Toast.makeText(this, "Audio subido exitosamente", Toast.LENGTH_SHORT).show();
                                    audioRef.getDownloadUrl().addOnSuccessListener(audioUrl -> {
                                        Toast.makeText(this, "URL de audio obtenida", Toast.LENGTH_SHORT).show();
                                        Map<String, Object> entrada = new HashMap<>();
                                        entrada.put("id", id);
                                        entrada.put("imagenUrl", imagenUrl.toString());
                                        entrada.put("audioUrl", audioUrl.toString());
                                        entrada.put("descripcion", descripcion);
                                        entrada.put("periodista", periodista);
                                        entrada.put("fecha", fecha);
                                        // guardar tambien base64 opcional
                                        String imagenBase64 = bitmapToBase64(imagenComprimida);
                                        if (imagenBase64 != null) entrada.put("imagenBase64", imagenBase64);

                                        databaseRef.child(id).setValue(entrada)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(this, "Datos subidos exitosamente", Toast.LENGTH_LONG).show();
                                                        limpiarCampos();
                                                        hideKeyboard();
                                                        new android.os.Handler().postDelayed(this::finish, 500);
                                                    } else {
                                                        Toast.makeText(this, "Error al subir datos: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al obtener URL del audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    });
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al subir el audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al obtener URL de la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Toast.makeText(this, "Subiendo imagen: " + (int) progress + "%", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarCampos() {
        descripciontxt.setText("");
        periodistatxt.setText("");
        ObjectoImagen.setImageBitmap(null);
        imagen = null;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

}
