package esteticaturnos.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Turno {

    private int id;
    private Clienta clienta;
    private Profesional profesional;
    private LocalDate fecha;
    private LocalTime hora;
    private String servicio;
    private String detalle;
    private EstadoTurno estado = EstadoTurno.PENDIENTE;
    private Double precio;

    public Turno() {
    }

    public Turno(int id, Clienta clienta, Profesional profesional, LocalDate fecha, LocalTime hora,
                 String servicio, String detalle, EstadoTurno estado, Double precio) {
        this.id = id;
        this.clienta = clienta;
        this.profesional = profesional;
        this.fecha = fecha;
        this.hora = hora;
        this.servicio = servicio;
        this.detalle = detalle;
        this.estado = estado;
        this.precio = precio;
    }

    public void cancelar() {
        if (estado == EstadoTurno.REALIZADO) {
            throw new IllegalStateException("No se puede cancelar un turno ya realizado");
        }
        estado = EstadoTurno.CANCELADO;
    }

    public void marcarRealizado(double precio) {
        if (estado == EstadoTurno.CANCELADO) {
            throw new IllegalStateException("No se puede realizar un turno cancelado");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        this.precio = precio;
        estado = EstadoTurno.REALIZADO;
    }

    public List<String> validar() {
        List<String> errores = new ArrayList<>();
        if (clienta == null) {
            errores.add("Seleccioná una clienta");
        }
        if (profesional == null) {
            errores.add("Seleccioná una profesional");
        }
        if (fecha == null) {
            errores.add("La fecha es obligatoria");
        } else if (!HorarioLaboral.esDiaHabil(fecha)) {
            errores.add("Solo se atiende de lunes a viernes");
        }
        if (hora == null || !HorarioLaboral.HORAS.contains(hora)) {
            errores.add("Elegí un horario entre las 09:00 y las 16:00");
        }
        if (!esServicioValido()) {
            errores.add("El servicio no corresponde al rubro de la profesional");
        }
        return errores;
    }

    private boolean esServicioValido() {
        return servicio != null && profesional != null && profesional.getRubro() != null
                && Arrays.asList(profesional.getRubro().getServicios()).contains(servicio);
    }

    public Rubro getRubro() {
        return profesional == null ? null : profesional.getRubro();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Clienta getClienta() { return clienta; }
    public void setClienta(Clienta clienta) { this.clienta = clienta; }

    public Profesional getProfesional() { return profesional; }
    public void setProfesional(Profesional profesional) { this.profesional = profesional; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public EstadoTurno getEstado() { return estado; }

    public Double getPrecio() { return precio; }
}