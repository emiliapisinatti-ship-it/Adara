package esteticaturnos.view;

import esteticaturnos.controller.ClientaController;
import esteticaturnos.controller.ValidacionException;
import esteticaturnos.model.Clienta;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FormClienta extends JDialog {

    private final ClientaController controller = new ClientaController();
    private final Clienta clienta;
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtTelefono = new JTextField(25);
    private final JTextArea txtObservaciones = new JTextArea(4, 25);

    public FormClienta(Window padre, Clienta clientaAEditar) {
        super(padre, clientaAEditar == null ? "Nueva clienta" : "Modificar clienta", ModalityType.APPLICATION_MODAL);
        this.clienta = clientaAEditar == null ? new Clienta() : clientaAEditar;
        construirPantalla();
        cargarDatos();
        pack();
        setLocationRelativeTo(padre);
    }

    public boolean isGuardado() {
        return guardado;
    }

    private void construirPantalla() {
        setLayout(new BorderLayout());
        add(Estilo.crearEncabezado(getTitle()), BorderLayout.NORTH);
        add(crearCampos(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    private JPanel crearCampos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(panel, c, 0, "Nombre y apellido *", txtNombre);
        agregarFila(panel, c, 1, "Teléfono *", txtTelefono);
        agregarFila(panel, c, 2, "Observaciones", new JScrollPane(txtObservaciones));
        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String etiqueta, Component campo) {
        c.gridx = 0;
        c.gridy = fila;
        c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        getRootPane().setDefaultButton(btnGuardar);
        panel.add(btnCancelar);
        panel.add(btnGuardar);
        return panel;
    }

    private void cargarDatos() {
        txtNombre.setText(clienta.getNombre());
        txtTelefono.setText(clienta.getTelefono());
        txtObservaciones.setText(clienta.getObservaciones());
    }

    private void guardar() {
        clienta.setNombre(txtNombre.getText());
        clienta.setTelefono(txtTelefono.getText());
        clienta.setObservaciones(txtObservaciones.getText().isBlank() ? null : txtObservaciones.getText().trim());
        try {
            controller.guardar(clienta);
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }
}