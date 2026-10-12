package esteticaturnos.dao;

import esteticaturnos.model.Clienta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientaDAO implements CrudDAO<Clienta> {

    private static final String INSERTAR =
            "INSERT INTO clientas (nombre, telefono, observaciones) VALUES (?, ?, ?) RETURNING id";
    private static final String MODIFICAR =
            "UPDATE clientas SET nombre = ?, telefono = ?, observaciones = ? WHERE id = ?";
    private static final String DAR_DE_BAJA =
            "UPDATE clientas SET activo = 0 WHERE id = ?";
    private static final String BUSCAR_POR_ID =
            "SELECT * FROM clientas WHERE id = ?";
    private static final String LISTAR_ACTIVAS =
            "SELECT * FROM clientas WHERE activo = 1 ORDER BY nombre";
       private static final String BUSCAR_POR_NOMBRE =
            "SELECT * FROM clientas WHERE (activo = 1 OR ? = 1) AND nombre LIKE ? ORDER BY activo DESC, nombre";
    private static final String REACTIVAR =
            "UPDATE clientas SET activo = 1 WHERE id = ?";

    @Override
    public void insertar(Clienta clienta) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(INSERTAR)) {
            sentencia.setString(1, clienta.getNombre());
            sentencia.setString(2, clienta.getTelefono());
            sentencia.setString(3, clienta.getObservaciones());
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                clienta.setId(resultado.getInt(1));
            }
        }
    }

    @Override
    public void modificar(Clienta clienta) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(MODIFICAR)) {
            sentencia.setString(1, clienta.getNombre());
            sentencia.setString(2, clienta.getTelefono());
            sentencia.setString(3, clienta.getObservaciones());
            sentencia.setInt(4, clienta.getId());
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
    public Clienta buscarPorId(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Clienta> listarActivos() throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(LISTAR_ACTIVAS)) {
            return listar(sentencia);
        }
    }

        public List<Clienta> buscarPorNombre(String texto, boolean incluirBajas) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(BUSCAR_POR_NOMBRE)) {
            sentencia.setInt(1, incluirBajas ? 1 : 0);
            sentencia.setString(2, "%" + texto.trim() + "%");
            return listar(sentencia);
        }
    }

    public void reactivar(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(REACTIVAR)) {
            sentencia.setInt(1, id);
            sentencia.executeUpdate();
        }
    }

    private List<Clienta> listar(PreparedStatement sentencia) throws SQLException {
        List<Clienta> clientas = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                clientas.add(mapear(resultado));
            }
        }
        return clientas;
    }

    private Clienta mapear(ResultSet resultado) throws SQLException {
        return new Clienta(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("telefono"),
                resultado.getString("observaciones"),
                resultado.getInt("activo") == 1);
    }

    private Connection conexion() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }
}