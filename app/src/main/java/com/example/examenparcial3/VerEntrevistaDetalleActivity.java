package com.example.examenparcial3;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;

public class VerEntrevistaDetalleActivity extends AppCompatActivity {

    private ImageView ivImagen;
    private TextView tvDescripcion, tvPeriodista, tvFecha;
    private Button btnReproducir, btnVolver;
    private MediaPlayer mediaPlayer;
    private String audioPathFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevista_detalle);

        ivImagen = findViewById(R.id.ivDetalleImagen);
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvPeriodista = findViewById(R.id.tvDetallePeriodista);
        tvFecha = findViewById(R.id.tvDetalleFecha);
        btnReproducir = findViewById(R.id.btnReproducirAudio);
        btnVolver = findViewById(R.id.btnVolverDetalle);

        String descripcion = getIntent().getStringExtra("descripcion");
        String periodista = getIntent().getStringExtra("periodista");
        String fecha = getIntent().getStringExtra("fecha");
        String imagenBase64 = getIntent().getStringExtra("imagenBase64");
        audioPathFirebase = getIntent().getStringExtra("audioPath");

        tvDescripcion.setText(descripcion);
        tvPeriodista.setText("Periodista: " + periodista);
        tvFecha.setText("Fecha: " + fecha);

        ivImagen.setImageBitmap(Base64StorageHelper.loadImageFromBase64(imagenBase64));

        btnReproducir.setOnClickListener(v -> reproducirAudio());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void reproducirAudio() {
        try {
            if (audioPathFirebase == null || audioPathFirebase.isEmpty()) {
                Toast.makeText(this, "No hay audio disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPathFirebase); // ruta real, NO Base64
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
