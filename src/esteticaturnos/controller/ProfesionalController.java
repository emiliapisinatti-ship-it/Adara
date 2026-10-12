package esteticaturnos.controller;

import esteticaturnos.dao.ProfesionalDAO;
import esteticaturnos.dao.TurnoDAO;
import esteticaturnos.model.Profesional;
import java.sql.SQLException;
import java.util.List;

public class ProfesionalController {

    private final ProfesionalDAO profesionalDAO = new ProfesionalDAO();
    private final TurnoDAO turnoDAO = new TurnoDAO();

    public List<Profesional> listar() throws SQLException {
        return profesionalDAO.listarActivos();
    }

    public List<Profesional> listar(boolean incluirBajas) throws SQLException {
        return profesionalDAO.listar(incluirBajas);
    }

    public void guardar(Profesional profesional) throws ValidacionException, SQLException {
        List<String> errores = profesional.validar();
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        if (profesional.getId() == 0) {
            profesionalDAO.insertar(profesional);
        } else {
            validarCambioDeRubro(profesional);
            profesionalDAO.modificar(profesional);
        }
    }

    public void darDeBaja(Profesional profesional) throws ValidacionException, SQLException {
        if (!profesional.isActivo()) {
            throw new ValidacionException(profesional.getNombre() + " ya está dada de baja.");
        }
        if (turnoDAO.tienePendientes(profesional.getId())) {
            throw new ValidacionException(
                    "La profesional tiene turnos pendientes. Cancelalos o reasignalos antes de darla de baja.");
        }
        profesionalDAO.darDeBaja(profesional.getId());
    }

    public void reactivar(Profesional profesional) throws ValidacionException, SQLException {
        if (profesional.isActivo()) {
            throw new ValidacionException(profesional.getNombre() + " ya está activa.");
        }
        profesionalDAO.reactivar(profesional.getId());
    }

    private void validarCambioDeRubro(Profesional profesional) throws ValidacionException, SQLException {
        Profesional guardada = profesionalDAO.buscarPorId(profesional.getId());
        boolean cambioElRubro = guardada != null && guardada.getRubro() != profesional.getRubro();
        if (cambioElRubro && turnoDAO.tienePendientes(profesional.getId())) {
            throw new ValidacionException(
                    "No se puede cambiar el rubro de una profesional con turnos pendientes.");
        }
    }
}
