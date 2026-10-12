package esteticaturnos;

import esteticaturnos.dao.EsquemaBD;
import esteticaturnos.view.Mensajes;
import esteticaturnos.view.VentanaPrincipal;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class EsteticaTurnos {

    public static void main(String[] args) {
        usarAspectoDelSistema();
        try {
            EsquemaBD.inicializar();
        } catch (SQLException e) {
            Mensajes.errorDeBase(null, e);
            return;
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private static void usarAspectoDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el aspecto del sistema: " + e.getMessage());
        }
    }
}