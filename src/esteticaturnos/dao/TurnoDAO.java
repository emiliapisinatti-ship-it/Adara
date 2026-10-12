package esteticaturnos.dao;

import esteticaturnos.model.Clienta;
import esteticaturnos.model.EstadoTurno;
import esteticaturnos.model.Profesional;
import esteticaturnos.model.Rubro;
import esteticaturnos.model.Turno;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoDAO implements CrudDAO<Turno> {

    private static final String SELECT_CON_DATOS = """
        SELECT t.id, t.fecha, t.hora, t.servicio, t.detalle, t.estado, t.precio,
               c.id AS c_id, c.nombre AS c_nombre, c.telefono AS c_telefono,
               c.observaciones AS c_observaciones, c.activo AS c_activo,
               p.id AS p_id, p.nombre AS p_nombre, p.telefono AS p_telefono,
               p.rubro AS p_rubro, p.activo AS p_activo
        FROM turnos t
        JOIN clientas c ON c.id = t.id_clienta
        JOIN profesionales p ON p.id = t.id_profesional
        """;

    private static final String INSERTAR = """
        INSERT INTO turnos (id_clienta, id_profesional, fecha, hora, servicio, detalle, estado, precio)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id""";
    private static final String MODIFICAR = """
        UPDATE turnos SET id_clienta = ?, id_profesional = ?, fecha = ?, hora = ?,
                          servicio = ?, detalle = ?, estado = ?, precio = ?
        WHERE id = ?""";
    private static final String DAR_DE_BAJA
            = "UPDATE turnos SET estado = 'CANCELADO' WHERE id = ? AND estado = 'PENDIENTE'";
    private static final String BUSCAR_POR_ID
            = SELECT_CON_DATOS + "WHERE t.id = ?";
    private static final String LISTAR_ACTIVOS
            = SELECT_CON_DATOS + "WHERE t.estado <> 'CANCELADO' ORDER BY t.fecha, t.hora";
    private static final String LISTAR_POR_CLIENTA
            = SELECT_CON_DATOS + "WHERE t.id_clienta = ? ORDER BY t.fecha DESC, t.hora DESC";
    private static final String AGENDA_DEL_DIA
            = SELECT_CON_DATOS + "WHERE t.id_profesional = ? AND t.fecha = ? AND t.estado <> 'CANCELADO' ORDER BY t.hora";
    private static final String PENDIENTES_ENTRE_FECHAS
            = SELECT_CON_DATOS + "WHERE t.id_profesional = ? AND t.estado = 'PENDIENTE' AND t.fecha BETWEEN ? AND ? "
            + "ORDER BY t.fecha, t.hora";
    private static final String HORAS_OCUPADAS = """
        SELECT hora FROM turnos
        WHERE fecha = ? AND id_profesional = ? AND estado <> 'CANCELADO' AND id <> ?
        ORDER BY hora""";
    private static final String CANCELAR_PENDIENTES_DE_CLIENTA
            = "UPDATE turnos SET estado = 'CANCELADO' WHERE id_clienta = ? AND estado = 'PENDIENTE'";
    private static final String CONTAR_PENDIENTES_DE_PROFESIONAL
            = "SELECT COUNT(*) FROM turnos WHERE id_profesional = ? AND estado = 'PENDIENTE'";

    @Override
    public void insertar(Turno turno) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(INSERTAR)) {
            cargarParametros(sentencia, turno);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                turno.setId(resultado.getInt(1));
            }
        }
    }

    @Override
    public void modificar(Turno turno) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(MODIFICAR)) {
            cargarParametros(sentencia, turno);
            sentencia.setInt(9, turno.getId());
            sentencia.executeUpdate();
        }
    }

    @Override
    public void darDeBaja(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(DAR_DE_BAJA)) {
            sentencia.setInt(1, id);
            sentencia.executeUpdate();
        }
    }

    @Override
    public Turno buscarPorId(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Turno> listarActivos() throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(LISTAR_ACTIVOS)) {
            return listar(sentencia);
        }
    }

    public List<Turno> listarPorClienta(int idClienta) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(LISTAR_POR_CLIENTA)) {
            sentencia.setInt(1, idClienta);
            return listar(sentencia);
        }
    }

    public List<Turno> listarAgendaDelDia(int idProfesional, LocalDate fecha) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(AGENDA_DEL_DIA)) {
            sentencia.setInt(1, idProfesional);
            sentencia.setString(2, fecha.toString());
            return listar(sentencia);
        }
    }

    public List<Turno> listarPendientesEntre(int idProfesional, LocalDate desde, LocalDate hasta)
            throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(PENDIENTES_ENTRE_FECHAS)) {
            sentencia.setInt(1, idProfesional);
            sentencia.setString(2, desde.toString());
            sentencia.setString(3, hasta.toString());
            return listar(sentencia);
        }
    }

    public List<LocalTime> horasOcupadas(LocalDate fecha, int idProfesional, int idTurnoExcluido)
            throws SQLException {
        List<LocalTime> horas = new ArrayList<>();
        try (PreparedStatement sentencia = conexion().prepareStatement(HORAS_OCUPADAS)) {
            sentencia.setString(1, fecha.toString());
            sentencia.setInt(2, idProfesional);
            sentencia.setInt(3, idTurnoExcluido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    horas.add(LocalTime.parse(resultado.getString("hora")));
                }
            }
        }
        return horas;
    }

    public void cancelarPendientesDeClienta(int idClienta) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(CANCELAR_PENDIENTES_DE_CLIENTA)) {
            sentencia.setInt(1, idClienta);
            sentencia.executeUpdate();
        }
    }

    public boolean tienePendientes(int idProfesional) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(CONTAR_PENDIENTES_DE_PROFESIONAL)) {
            sentencia.setInt(1, idProfesional);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.getInt(1) > 0;
            }
        }
    }

    private void cargarParametros(PreparedStatement sentencia, Turno turno) throws SQLException {
        sentencia.setInt(1, turno.getClienta().getId());
        sentencia.setInt(2, turno.getProfesional().getId());
        sentencia.setString(3, turno.getFecha().toString());
        sentencia.setString(4, turno.getHora().toString());
        sentencia.setString(5, turno.getServicio());
        sentencia.setString(6, turno.getDetalle());
        sentencia.setString(7, turno.getEstado().name());
        if (turno.getPrecio() == null) {
            sentencia.setNull(8, Types.REAL);
        } else {
            sentencia.setDouble(8, turno.getPrecio());
        }
    }

    private List<Turno> listar(PreparedStatement sentencia) throws SQLException {
        List<Turno> turnos = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                turnos.add(mapear(resultado));
            }
        }
        return turnos;
    }

    private Turno mapear(ResultSet resultado) throws SQLException {
        Clienta clienta = new Clienta(
                resultado.getInt("c_id"),
                resultado.getString("c_nombre"),
                resultado.getString("c_telefono"),
                resultado.getString("c_observaciones"),
                resultado.getInt("c_activo") == 1);
        Profesional profesional = new Profesional(
                resultado.getInt("p_id"),
                resultado.getString("p_nombre"),
                resultado.getString("p_telefono"),
                Rubro.valueOf(resultado.getString("p_rubro")),
                resultado.getInt("p_activo") == 1);
        Double precio = resultado.getObject("precio") == null ? null : resultado.getDouble("precio");
        return new Turno(
                resultado.getInt("id"),
                clienta,
                profesional,
                LocalDate.parse(resultado.getString("fecha")),
                LocalTime.parse(resultado.getString("hora")),
                resultado.getString("servicio"),
                resultado.getString("detalle"),
                EstadoTurno.valueOf(resultado.getString("estado")),
                precio);
    }

    private Connection conexion() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }
}
