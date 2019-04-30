package com.example.pomodoro.models;

public class Project {

    private String nombre;
    private String estado;

    private String key;

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

    /**
     * Obtener la clave
     * @return - la clave
     */
    public String getKey(){
        return key;
    }

    /**
     * Set the new key
     * @param key
     */
    public void setKey(String key){
        this.key = key;
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

        Project a = (Project) o;
        String otherKey = a.getKey();
        if (key.equals(otherKey)) {
            return true;
        }else{
            return false;
        }
    }

}
