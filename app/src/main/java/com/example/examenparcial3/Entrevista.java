package com.example.examenparcial3;

import android.os.Parcel;
import android.os.Parcelable;

public class Entrevista {
    private String id;
    private String imagenUrl;
    private String audioUrl;
    private String descripcion;
    private String periodista;
    private String fecha;

    public Entrevista() {

    }

    public Entrevista(String id, String imagenUrl, String audioUrl, String descripcion, String fecha, String periodista) {
        this.id = id;
        this.imagenUrl = imagenUrl;
        this.audioUrl = audioUrl;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.periodista = periodista;

    }

    //
    protected Entrevista(Parcel in) {
        id = in.readString();
        imagenUrl = in.readString();
        audioUrl = in.readString();
        descripcion = in.readString();
        fecha = in.readString();

    }

    public static final Parcelable.Creator<Entrevista> CREATOR = new Parcelable.Creator<Entrevista>() {
        @Override
        public Entrevista createFromParcel(Parcel in) {
            return new Entrevista(in);
        }

        @Override
        public Entrevista[] newArray(int size) {
            return new Entrevista[size];
        }
    };


    public int describeContents() {
        return 0;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(imagenUrl);
        dest.writeString(audioUrl);
        dest.writeString(descripcion);
        dest.writeString(fecha);
    }
    //

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getperiodista() {
        return periodista;
    }

    public void setperiodista(String periodista) {
        this.periodista = periodista;
    }

    @Override
    public String toString() {
        return descripcion + " - " + fecha;
    }
}
