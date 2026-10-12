package esteticaturnos.controller;

import esteticaturnos.dao.TurnoDAO;
import esteticaturnos.model.EstadoTurno;
import esteticaturnos.model.HorarioLaboral;
import esteticaturnos.model.Profesional;
import esteticaturnos.model.Turno;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoController {

    private final TurnoDAO turnoDAO = new TurnoDAO();
    public List<Turno> listarPorClienta(int idClienta) throws SQLException {
        return turnoDAO.listarPorClienta(idClienta);
    }

    public List<Turno> agendaDelDia(Profesional profesional, LocalDate fecha) throws SQLException {
        return turnoDAO.listarAgendaDelDia(profesional.getId(), fecha);
    }

    public List<Turno> proximosTurnos(Profesional profesional, int dias) throws SQLException {
        LocalDate hoy = LocalDate.now();
        return turnoDAO.listarPendientesEntre(profesional.getId(), hoy, hoy.plusDays(dias));
    }

    public List<LocalTime> horasDisponibles(LocalDate fecha, Profesional profesional, int idTurnoExcluido)
            throws SQLException {
        if (fecha == null || profesional == null || !HorarioLaboral.esDiaHabil(fecha)) {
            return List.of();
        }
        List<LocalTime> disponibles = new ArrayList<>(HorarioLaboral.HORAS);
        disponibles.removeAll(turnoDAO.horasOcupadas(fecha, profesional.getId(), idTurnoExcluido));
        if (fecha.equals(LocalDate.now())) {
            disponibles.removeIf(hora -> !hora.isAfter(LocalTime.now()));
        }
        return disponibles;
    }

    public void guardar(Turno turno) throws ValidacionException, SQLException {
        List<String> errores = turno.validar();
        if (turno.getId() == 0 && esEnElPasado(turno)) {
            errores.add("No se pueden cargar turnos en fechas u horarios que ya pasaron");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            throw new ValidacionException("Solo se pueden modificar turnos pendientes");
        }
        validarHorarioLibre(turno);
        if (turno.getId() == 0) {
            turnoDAO.insertar(turno);
        } else {
            turnoDAO.modificar(turno);
        }
    }

    public void cancelar(Turno turno) throws ValidacionException, SQLException {
        try {
            turno.cancelar();
        } catch (IllegalStateException e) {
            throw new ValidacionException(e.getMessage());
        }
        turnoDAO.darDeBaja(turno.getId());
    }

    public void marcarRealizado(Turno turno, double precio) throws ValidacionException, SQLException {
        try {
            turno.marcarRealizado(precio);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
        turnoDAO.modificar(turno);
    }

    public double calcularTotalGastado(List<Turno> turnos) {
        return turnos.stream()
                .filter(turno -> turno.getEstado() == EstadoTurno.REALIZADO)
                .mapToDouble(Turno::getPrecio)
                .sum();
    }

    private boolean esEnElPasado(Turno turno) {
        if (turno.getFecha() == null || turno.getHora() == null) {
            return false;
        }
        return turno.getFecha().atTime(turno.getHora()).isBefore(LocalDateTime.now());
    }

    private void validarHorarioLibre(Turno turno) throws ValidacionException, SQLException {
        List<LocalTime> ocupadas = turnoDAO.horasOcupadas(
                turno.getFecha(), turno.getProfesional().getId(), turno.getId());
        if (ocupadas.contains(turno.getHora())) {
            throw new ValidacionException("La profesional ya tiene un turno el "
                    + turno.getFecha() + " a las " + turno.getHora() + ". Elegí otro horario.");
        }
    }
}