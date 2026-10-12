package esteticaturnos.model;

public class Clienta extends Persona {

    private String observaciones;

    public Clienta() {
    }

    public Clienta(int id, String nombre, String telefono, String observaciones, boolean activo) {
        super(id, nombre, telefono, activo);
        this.observaciones = observaciones;
    }

    @Override
    public String getDescripcion() {
        return getNombre() + " – " + getTelefono();
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}