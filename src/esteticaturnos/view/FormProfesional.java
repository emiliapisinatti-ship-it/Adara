package esteticaturnos.view;

import esteticaturnos.controller.ProfesionalController;
import esteticaturnos.controller.ValidacionException;
import esteticaturnos.model.Profesional;
import esteticaturnos.model.Rubro;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FormProfesional extends JDialog {

    private final ProfesionalController controller = new ProfesionalController();
    private final Profesional profesional;
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtTelefono = new JTextField(25);
    private final JComboBox<Rubro> cboRubro = new JComboBox<>(Rubro.values());

    public FormProfesional(Window padre, Profesional profesionalAEditar) {
        super(padre, profesionalAEditar == null ? "Nueva profesional" : "Modificar profesional",
                ModalityType.APPLICATION_MODAL);
        this.profesional = profesionalAEditar == null ? new Profesional() : profesionalAEditar;
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
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(panel, c, 0, "Nombre y apellido *", txtNombre);
        agregarFila(panel, c, 1, "Teléfono *", txtTelefono);
        agregarFila(panel, c, 2, "Rubro *", cboRubro);
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
        txtNombre.setText(profesional.getNombre());
        txtTelefono.setText(profesional.getTelefono());
        if (profesional.getRubro() != null) {
            cboRubro.setSelectedItem(profesional.getRubro());
        }
    }

    private void guardar() {
        Rubro rubroAnterior = profesional.getRubro();
        profesional.setNombre(txtNombre.getText());
        profesional.setTelefono(txtTelefono.getText());
        profesional.setRubro((Rubro) cboRubro.getSelectedItem());
        try {
            controller.guardar(profesional);
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            profesional.setRubro(rubroAnterior);
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }
}