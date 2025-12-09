package com.example.examenparcial3;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.w("MyApp", "FirebaseApp initializeApp error: " + e.getMessage());
        }

        // URL realtime database
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://examenp3firebase-default-rtdb.firebaseio.com/"
        );

        databaseReference = database.getReference();

        // Inicializamos Storage
        FirebaseStorageHelper.initializeStorage();
    }

    public static DatabaseReference getDatabaseReference(String child) {
        return databaseReference.child(child);
    }
}
