package esteticaturnos.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public final class Estilo {

    public static final Color PRIMARIO = new Color(138, 53, 88);
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font SUBTITULO = new Font("SansSerif", Font.BOLD, 14);

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_FECHA_CON_DIA
            = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.of("es", "AR"));
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    private Estilo() {
    }

    public static JPanel crearEncabezado(String texto) {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel titulo = new JLabel(texto);
        titulo.setFont(TITULO);
        titulo.setForeground(Color.WHITE);
        encabezado.add(titulo, BorderLayout.WEST);
        return encabezado;
    }

    public static void configurarTabla(JTable tabla) {
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    public static String formatearPrecio(Double precio) {
        return precio == null ? "—" : FORMATO_PRECIO.format(precio);
    }
}
