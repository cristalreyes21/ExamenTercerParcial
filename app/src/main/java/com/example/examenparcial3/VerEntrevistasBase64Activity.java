package com.example.examenparcial3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerEntrevistasBase64Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrevistaBase64Adapter adapter;
    private List<EntrevistaBase64> entrevistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevistas_base64);

        recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        entrevistas = new ArrayList<>();
        adapter = new EntrevistaBase64Adapter(entrevistas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        cargarEntrevistas();

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    private void cargarEntrevistas() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");

        dbRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entrevistas.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Object raw = snap.getValue();
                    if (!(raw instanceof Map)) continue;

                    Map<String, Object> data = (Map<String, Object>) raw;

                    EntrevistaBase64 entrevista = new EntrevistaBase64();
                    entrevista.setId(getValue(data, "id"));
                    entrevista.setDescripcion(getValue(data, "descripcion"));
                    entrevista.setPeriodista(getValue(data, "periodista"));
                    entrevista.setFecha(getValue(data, "fecha"));
                    entrevista.setImagenBase64(getValue(data, "imagenBase64"));
                    entrevista.setAudioPath(getValue(data, "audioUrl"));


                    entrevistas.add(entrevista);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VerEntrevistasBase64Activity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getValue(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v != null ? v.toString() : "";
    }

    public static class EntrevistaBase64 {
        private String id;
        private String descripcion;
        private String periodista;
        private String fecha;
        private String imagenBase64;
        private String audioPath;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getPeriodista() { return periodista; }
        public void setPeriodista(String periodista) { this.periodista = periodista; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public String getImagenBase64() { return imagenBase64; }
        public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
        public String getAudioPath() { return audioPath; }
        public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
    }

    private class EntrevistaBase64Adapter extends RecyclerView.Adapter<EntrevistaBase64Adapter.ViewHolder> {

        private List<EntrevistaBase64> entrevistas;

        public EntrevistaBase64Adapter(List<EntrevistaBase64> entrevistas) {
            this.entrevistas = entrevistas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrevista_base64, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            EntrevistaBase64 e = entrevistas.get(pos);

            holder.tvDescripcion.setText(e.getDescripcion());
            holder.tvPeriodista.setText("Periodista: " + e.getPeriodista());
            holder.tvFecha.setText("Fecha: " + e.getFecha());

            Bitmap img = Base64StorageHelper.loadImageFromBase64(e.getImagenBase64());
            holder.ivImagen.setImageBitmap(img);

            holder.ivImagen.setOnClickListener(v -> abrirDetalles(e));

            holder.btnEditar.setOnClickListener(v -> abrirEditor(e));

            holder.btnEliminar.setOnClickListener(v -> eliminarEntrevista(e));
        }

        @Override
        public int getItemCount() {
            return entrevistas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImagen;
            TextView tvDescripcion, tvPeriodista, tvFecha;
            Button btnEditar, btnEliminar;

            ViewHolder(View item) {
                super(item);
                ivImagen = item.findViewById(R.id.ivImagen);
                tvDescripcion = item.findViewById(R.id.tvDescripcion);
                tvPeriodista = item.findViewById(R.id.tvPeriodista);
                tvFecha = item.findViewById(R.id.tvFecha);
                btnEditar = item.findViewById(R.id.btnEditar);
                btnEliminar = item.findViewById(R.id.btnEliminar);
            }
        }
    }

    private void abrirDetalles(EntrevistaBase64 e) {
        Intent i = new Intent(this, VerEntrevistaDetalleActivity.class);
        i.putExtra("id", e.getId());
        i.putExtra("descripcion", e.getDescripcion());
        i.putExtra("periodista", e.getPeriodista());
        i.putExtra("fecha", e.getFecha());
        i.putExtra("imagenBase64", e.getImagenBase64());
        i.putExtra("audioPath", e.getAudioPath());
        startActivity(i);
    }

    private void abrirEditor(EntrevistaBase64 e) {
        Intent i = new Intent(this, EditarEntrevistaActivity.class);
        i.putExtra("id", e.getId());
        i.putExtra("descripcion", e.getDescripcion());
        i.putExtra("periodista", e.getPeriodista());
        startActivity(i);
    }

    private void eliminarEntrevista(EntrevistaBase64 e) {
        FirebaseDatabase.getInstance()
                .getReference("entrevistas")
                .child(e.getId())
                .removeValue()
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Eliminada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err ->
                        Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }
}
