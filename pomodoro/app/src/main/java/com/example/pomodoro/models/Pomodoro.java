package com.example.pomodoro.models;

import java.sql.Timestamp;

public class Pomodoro {

    private Boolean empezado;
    private Boolean enDescanso;
    private Timestamp horaFin;
    private String proyecto;
    private int relax;
    private int work;
    private String usuario;

    private String key;

    public Pomodoro(){

    }

    // Getter and setters

    public Boolean getEnDescanso() {
        return enDescanso;
    }

    public void setEnDescanso(Boolean enDescanso) {
        this.enDescanso = enDescanso;
    }

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

    public void setHoraFin(Timestamp horaFin) {
        this.horaFin = horaFin;
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

    public Timestamp getHoraFin() {
        return horaFin;
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

    /**
     * Overwrite equals method
     * @param o - the object to compare
     * @return - true if equals
     */
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o == this) return true;

        Pomodoro a = (Pomodoro) o;
        String otherKey = a.getKey();
        if (key.equals(otherKey)) {
            return true;
        }else{
            return false;
        }
    }
}
