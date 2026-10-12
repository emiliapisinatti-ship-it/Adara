package esteticaturnos.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Persona {

    private int id;
    private String nombre;
    private String telefono;
    private boolean activo = true;

    protected Persona() {
    }

    protected Persona(int id, String nombre, String telefono, boolean activo) {
        this.id = id;
        setNombre(nombre);
        setTelefono(telefono);
        this.activo = activo;
    }

    public abstract String getDescripcion();

    public List<String> validar() {
        List<String> errores = new ArrayList<>();
        if (estaVacio(nombre)) {
            errores.add("El nombre es obligatorio");
        }
        if (estaVacio(telefono)) {
            errores.add("El teléfono es obligatorio");
        }
        return errores;
    }

    protected static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    @Override
    public String toString() {
        return getDescripcion();
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
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono == null ? null : telefono.trim();
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}