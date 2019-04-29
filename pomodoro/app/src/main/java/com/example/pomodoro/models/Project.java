package com.example.pomodoro.models;

public class Project {

    private String nombre;
    private int numeroMiembros;
    private String estado;

    public Project(){

    }

    /**
     * Obtener el nombre
     * @return - el nombre
     */
    public String getNombre(){
        return nombre;
    }

    /**
     * Cambiar el nombre
     * @param nombre - el nuevo nombre
     */
    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    /**
     * Obtener el número de miembros del proyecto
     * @return - el número
     */
    public int getNumeroMiembros(){
        return numeroMiembros;
    }

    /**
     * Cambiar el número de miembros del proyecto
     * @param numeroMiembros - el número de miembros
     */
    public void setNumeroMiembros(int numeroMiembros){
        this.numeroMiembros = numeroMiembros;
    }

    /**
     * Obtener estado
     * @return
     */
    public String getEstado(){
        return estado;
    }

    /**
     * Cambiar estado
     * @param estado - el nuevo estado
     */
    public void setEstado(String estado){
        this.estado = estado;
    }
}
