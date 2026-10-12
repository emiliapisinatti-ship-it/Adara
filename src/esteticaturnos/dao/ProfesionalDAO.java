package esteticaturnos.dao;

import esteticaturnos.model.Profesional;
import esteticaturnos.model.Rubro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfesionalDAO implements CrudDAO<Profesional> {

    private static final String INSERTAR
            = "INSERT INTO profesionales (nombre, telefono, rubro) VALUES (?, ?, ?) RETURNING id";
    private static final String MODIFICAR
            = "UPDATE profesionales SET nombre = ?, telefono = ?, rubro = ? WHERE id = ?";
    private static final String DAR_DE_BAJA
            = "UPDATE profesionales SET activo = 0 WHERE id = ?";
    private static final String BUSCAR_POR_ID
            = "SELECT * FROM profesionales WHERE id = ?";
    private static final String LISTAR_ACTIVAS
            = "SELECT * FROM profesionales WHERE activo = 1 ORDER BY nombre";
    private static final String LISTAR
            = "SELECT * FROM profesionales WHERE (activo = 1 OR ? = 1) ORDER BY activo DESC, nombre";
    private static final String REACTIVAR
            = "UPDATE profesionales SET activo = 1 WHERE id = ?";

    @Override
    public void insertar(Profesional profesional) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(INSERTAR)) {
            sentencia.setString(1, profesional.getNombre());
            sentencia.setString(2, profesional.getTelefono());
            sentencia.setString(3, profesional.getRubro().name());
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                profesional.setId(resultado.getInt(1));
            }
        }
    }

    @Override
    public void modificar(Profesional profesional) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(MODIFICAR)) {
            sentencia.setString(1, profesional.getNombre());
            sentencia.setString(2, profesional.getTelefono());
            sentencia.setString(3, profesional.getRubro().name());
            sentencia.setInt(4, profesional.getId());
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
    public Profesional buscarPorId(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Profesional> listarActivos() throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(LISTAR_ACTIVAS)) {
            return listar(sentencia);
        }
    }

    public List<Profesional> listar(boolean incluirBajas) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(LISTAR)) {
            sentencia.setInt(1, incluirBajas ? 1 : 0);
            return listar(sentencia);
        }
    }

    public void reactivar(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion().prepareStatement(REACTIVAR)) {
            sentencia.setInt(1, id);
            sentencia.executeUpdate();
        }
    }

    private List<Profesional> listar(PreparedStatement sentencia) throws SQLException {
        List<Profesional> profesionales = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                profesionales.add(mapear(resultado));
            }
        }
        return profesionales;
    }

    private Profesional mapear(ResultSet resultado) throws SQLException {
        return new Profesional(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("telefono"),
                Rubro.valueOf(resultado.getString("rubro")),
                resultado.getInt("activo") == 1);
    }

    private Connection conexion() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }
}
