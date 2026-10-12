package esteticaturnos.model;

public enum Rubro {

    PESTANIAS("Pestañas", "Pelo a pelo", "Volumen ruso", "Lifting", "Service", "Retiro"),
    UNIAS("Uñas", "Semipermanente", "Kapping", "Soft gel", "Esculpidas", "Service", "Retiro");

    private final String nombre;
    private final String[] servicios;

    Rubro(String nombre, String... servicios) {
        this.nombre = nombre;
        this.servicios = servicios;
    }

    public String getNombre() {
        return nombre;
    }

    public String[] getServicios() {
        return servicios.clone();
    }

    @Override
    public String toString() {
        return nombre;
    }
}