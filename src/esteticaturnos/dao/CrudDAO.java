package esteticaturnos.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T> {

    void insertar(T entidad) throws SQLException;

    void modificar(T entidad) throws SQLException;

    void darDeBaja(int id) throws SQLException;

    T buscarPorId(int id) throws SQLException;

    List<T> listarActivos() throws SQLException;
}