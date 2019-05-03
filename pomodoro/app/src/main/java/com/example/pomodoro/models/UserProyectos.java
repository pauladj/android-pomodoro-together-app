package com.example.pomodoro.models;

import java.util.ArrayList;

public class UserProyectos {

    private String proyecto;
    private String usuario;

    public UserProyectos(){

    }

    /**
     * Obtener la key del proyecto
     * @return - la key del proyecto
     */
    public String getProyecto(){
        return proyecto;
    }

    /**
     * Cambiar la key del proyecto
      * @param proyecto - la nueva key del proyecto
     */
    public void setProyecto(String proyecto){
        this.proyecto = proyecto;
    }

    /**
     * Cambiar el usuario
     * @param usuario
     */
    public void setUsuario(String usuario){
        this.usuario = usuario;
    }

    /**
     * Obtener el usuario
     */
    public String getUsuario(){
        return usuario;
    }
}
