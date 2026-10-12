package esteticaturnos.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        super("Adara — Gestión de turnos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
                add(Estilo.crearEncabezado("Adara · Estética"), BorderLayout.NORTH);
        add(crearPestanias(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
    }

    private JTabbedPane crearPestanias() {
        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("Clientas", new PanelClientas());
        pestanias.addTab("Profesionales", new PanelProfesionales());
        return pestanias;
    }
}