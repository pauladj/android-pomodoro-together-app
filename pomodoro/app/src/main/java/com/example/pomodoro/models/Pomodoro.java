package com.example.pomodoro.models;

import com.google.api.client.util.DateTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Pomodoro {

    private Boolean empezado;
    private String horaWorkFin;
    private String horaDescansoFin;
    private String proyecto;
    private int relax;
    private int work;
    private String usuario;

    private String key;

    public Pomodoro() {

    }

    // Getter and setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getEmpezado() {
        return empezado;
    }

    public void setEmpezado(Boolean empezado) {
        this.empezado = empezado;
    }

    public void setProyecto(String proyecto) {
        this.proyecto = proyecto;
    }

    public void setRelax(int relax) {
        this.relax = relax;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getHoraDescansoFin() {
        return horaDescansoFin;
    }

    public String getHoraWorkFin() {
        return horaWorkFin;
    }

    public String getProyecto() {
        return proyecto;
    }

    public int getRelax() {
        return relax;
    }

    public int getWork() {
        return work;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setHoraDescansoFin(String horaDescansoFin) {
        this.horaDescansoFin = horaDescansoFin;
    }

    public void setHoraWorkFin(String horaWorkFin) {
        this.horaWorkFin = horaWorkFin;
    }

    /**
     * Overwrite equals method
     *
     * @param o - the object to compare
     * @return - true if equals
     */
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        Pomodoro a = (Pomodoro) o;
        String otherKey = a.getKey();
        if (key.equals(otherKey)) {
            return true;
        } else {
            return false;
        }
    }
}
