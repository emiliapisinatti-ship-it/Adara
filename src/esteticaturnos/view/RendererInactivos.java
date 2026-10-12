package esteticaturnos.view;

import java.awt.Color;
import java.awt.Component;
import java.util.function.IntPredicate;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class RendererInactivos extends DefaultTableCellRenderer {

    private static final Color GRIS = new Color(150, 150, 150);

    private final IntPredicate filaDadaDeBaja;

    public RendererInactivos(IntPredicate filaDadaDeBaja) {
        this.filaDadaDeBaja = filaDadaDeBaja;
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean conFoco, int fila, int columna) {
        Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, conFoco, fila, columna);
        if (!seleccionada) {
            celda.setForeground(filaDadaDeBaja.test(fila) ? GRIS : tabla.getForeground());
        }
        return celda;
    }
}