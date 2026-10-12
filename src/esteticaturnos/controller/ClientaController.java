package esteticaturnos.controller;

import esteticaturnos.dao.ClientaDAO;
import esteticaturnos.dao.TurnoDAO;
import esteticaturnos.model.Clienta;
import java.sql.SQLException;
import java.util.List;

public class ClientaController {

    private final ClientaDAO clientaDAO = new ClientaDAO();
    private final TurnoDAO turnoDAO = new TurnoDAO();
    public List<Clienta> buscar(String texto, boolean incluirBajas) throws SQLException {
        return clientaDAO.buscarPorNombre(texto == null ? "" : texto, incluirBajas);
    }

    public void guardar(Clienta clienta) throws ValidacionException, SQLException {
        List<String> errores = clienta.validar();
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        if (clienta.getId() == 0) {
            clientaDAO.insertar(clienta);
        } else {
            clientaDAO.modificar(clienta);
        }
    }

    public void darDeBaja(Clienta clienta) throws ValidacionException, SQLException {
        if (!clienta.isActivo()) {
            throw new ValidacionException(clienta.getNombre() + " ya está dada de baja.");
        }
        turnoDAO.cancelarPendientesDeClienta(clienta.getId());
        clientaDAO.darDeBaja(clienta.getId());
    }

    public void reactivar(Clienta clienta) throws ValidacionException, SQLException {
        if (clienta.isActivo()) {
            throw new ValidacionException(clienta.getNombre() + " ya está activa.");
        }
        clientaDAO.reactivar(clienta.getId());
    }
}