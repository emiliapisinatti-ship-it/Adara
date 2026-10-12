package esteticaturnos.view;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class Mensajes {

    private Mensajes() {
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Revisá los datos", JOptionPane.WARNING_MESSAGE);
    }

    public static void errorDeBase(Component padre, Exception error) {
        JOptionPane.showMessageDialog(padre,
                "No se pudo acceder a la base de datos.\nDetalle: " + error.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }
}