package com.example.examenparcial3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EntrevistaAdapter extends BaseAdapter {

    private Context context;
    private List<Entrevista> entrevistas;
    private LayoutInflater inflater;

    public EntrevistaAdapter(Context context, List<Entrevista> entrevistas) {
        this.context = context;
        this.entrevistas = entrevistas;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return entrevistas.size();
    }

    @Override
    public Object getItem(int position) {
        return entrevistas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_entrevista, parent, false);
        }

        ImageView imagenEntrevista = convertView.findViewById(R.id.imagenEntrevista);
        TextView descripcionEntrevista = convertView.findViewById(R.id.descripcionEntrevista);
        TextView fechaEntrevista = convertView.findViewById(R.id.fechaEntrevista);

        Entrevista entrevista = entrevistas.get(position);

        // Cargar la imagen utilizando
        Glide.with(context).load(entrevista.getImagenUrl()).into(imagenEntrevista);

        // Mostrar la descripción y la fecha
        descripcionEntrevista.setText("Periodista:"+entrevista.getperiodista()+"\n"+entrevista.getDescripcion());
        fechaEntrevista.setText(entrevista.getFecha());

        return convertView;
    }
}
