package com.example.demo01.activities.models;

import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Familia implements Serializable {

    private int id;
    private String nombre, descripcion, clave, idCreador, idFamilia, uriFamilia;
    private List<String> miembros;
    private Uri imgFamilia;
    private Date fecha;
    //private Uri uriFamilia;

    public Familia() {
    }

    public List<String> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<String> miembros) {
        this.miembros = miembros;
    }

    public Uri getImgFamilia() {
        return imgFamilia;
    }

    public void setImgFamilia(Uri imgFamilia) {
        this.imgFamilia = imgFamilia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(String idCreador) {
        this.idCreador = idCreador;
    }

    public String getIdFamilia() {
        return idFamilia;
    }

    public void setIdFamilia(String idFamilia) {
        this.idFamilia = idFamilia;
    }

    public String getUriFamilia() {
        return uriFamilia;
    }

    public void setUriFamilia(String uriFamilia) {
        this.uriFamilia = uriFamilia;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
