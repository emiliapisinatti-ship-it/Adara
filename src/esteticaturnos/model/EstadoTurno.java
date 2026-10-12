package esteticaturnos.model;

public enum EstadoTurno {

    PENDIENTE("Pendiente"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado");

    private final String nombre;

    EstadoTurno(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}