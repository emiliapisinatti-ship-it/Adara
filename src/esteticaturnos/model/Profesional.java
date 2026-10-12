package esteticaturnos.model;

import java.util.List;

public class Profesional extends Persona {

    private Rubro rubro;

    public Profesional() {
    }

    public Profesional(int id, String nombre, String telefono, Rubro rubro, boolean activo) {
        super(id, nombre, telefono, activo);
        this.rubro = rubro;
    }

    @Override
    public String getDescripcion() {
        return rubro == null ? getNombre() : getNombre() + " (" + rubro.getNombre() + ")";
    }

    @Override
    public List<String> validar() {
        List<String> errores = super.validar();
        if (rubro == null) {
            errores.add("El rubro es obligatorio");
        }
        return errores;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }
}