package com.example.examenparcial3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class EditarEntrevistaActivity extends AppCompatActivity {

    private EditText etDescripcion, etPeriodista;
    private Button btnGuardar, btnCancelar;
    private String idEntrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_entrevista);

        etDescripcion = findViewById(R.id.etEditarDescripcion);
        etPeriodista = findViewById(R.id.etEditarPeriodista);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnCancelar = findViewById(R.id.btnCancelarEdicion);

        idEntrevista = getIntent().getStringExtra("id");
        etDescripcion.setText(getIntent().getStringExtra("descripcion"));
        etPeriodista.setText(getIntent().getStringExtra("periodista"));

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarCambios() {
        String nuevaDescripcion = etDescripcion.getText().toString();
        String nuevoPeriodista = etPeriodista.getText().toString();

        FirebaseDatabase.getInstance()
                .getReference("entrevistas")
                .child(idEntrevista)
                .child("descripcion")
                .setValue(nuevaDescripcion);

        FirebaseDatabase.getInstance()
                .getReference("entrevistas")
                .child(idEntrevista)
                .child("periodista")
                .setValue(nuevoPeriodista)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(err ->
                        Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }
}
