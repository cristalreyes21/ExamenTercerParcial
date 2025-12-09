package com.example.examenparcial3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button btnentrevistar,btnmostrar;
    
    // Referencia a Firebase Realtime Database
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Autenticado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(this, "Error al autenticar: " + (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                        Log.e("AuthDebug", "Anonymous sign-in failed", e);
                        if (e != null) e.printStackTrace();
                    }
                });


        btnentrevistar = (Button) findViewById(R.id.btnCrearEntrevista);
        btnentrevistar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearentrevista();
            }
        });

        // Botón para ver entrevistas con Base64
        Button btnVerEntrevistasBase64 = findViewById(R.id.btnVerEntrevistasBase64);
        btnVerEntrevistasBase64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verEntrevistasBase64();
            }
        });

        // Inicializar Firebase Realtime Database
        inicializarFirebase();
    }

    // Inicializar la conexión con Firebase
    private void inicializarFirebase() {
        try {
            // Obtener la instancia de Firebase Database con tu URL específica
            database = FirebaseDatabase.getInstance("https://examenp3firebase-default-rtdb.firebaseio.com/").getReference();
            
            // Verificar la conexión (opcional)
            database.child("test").child("conexion").setValue("conectado")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Firebase conectado exitosamente a tu base de datos", Toast.LENGTH_SHORT).show();
                    // Limpiar el test
                    database.child("test").removeValue();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión a Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                
        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //funciones
    private void crearentrevista(){
        Intent intent = new Intent(MainActivity.this,CrearEntrvistaActivity.class);
        startActivity(intent);
    }

    private void verEntrevistasBase64(){
        Intent intent = new Intent(MainActivity.this,VerEntrevistasBase64Activity.class);
        startActivity(intent);
    }

}