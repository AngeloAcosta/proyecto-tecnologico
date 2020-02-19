package com.example.demo01.activities.models;

import android.media.Image;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Actividad implements Serializable {
    private String destino, idActividad, nombre, detalle, recompensa, uriImagen, idGrupoFamiliar, idCreador, condicion, estado, idDestino, prioridad, fechaInicio, fechaFin, horaInicio, horaFin;

    private Date fecha;
    private int puntos;

    public Actividad() {
    }

    public Actividad(String destino, String idActividad, String nombre, String detalle, String recompensa, String uriImagen, String idGrupoFamiliar, String idCreador, String condicion, String estado, String idDestino, String prioridad, String fechaInicio, String fechaFin, String horaInicio, String horaFin, Date fecha, int puntos) {
        this.destino = destino;
        this.idActividad = idActividad;
        this.nombre = nombre;
        this.detalle = detalle;
        this.recompensa = recompensa;
        this.uriImagen = uriImagen;
        this.idGrupoFamiliar = idGrupoFamiliar;
        this.idCreador = idCreador;
        this.condicion = condicion;
        this.estado = estado;
        this.idDestino = idDestino;
        this.prioridad = prioridad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.fecha = fecha;
        this.puntos = puntos;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(String idDestino) {
        this.idDestino = idDestino;
    }

    public String getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(String idActividad) {
        this.idActividad = idActividad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(String recompensa) {
        this.recompensa = recompensa;
    }

    public String getUriImagen() {
        return uriImagen;
    }

    public void setUriImagen(String uriImagen) {
        this.uriImagen = uriImagen;
    }

    public String getIdGrupoFamiliar() {
        return idGrupoFamiliar;
    }

    public void setIdGrupoFamiliar(String idGrupoFamiliar) {
        this.idGrupoFamiliar = idGrupoFamiliar;
    }

    public String getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(String idCreador) {
        this.idCreador = idCreador;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }


    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}
