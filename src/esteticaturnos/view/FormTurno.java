package esteticaturnos.view;

import esteticaturnos.controller.ProfesionalController;
import esteticaturnos.controller.TurnoController;
import esteticaturnos.controller.ValidacionException;
import esteticaturnos.model.Clienta;
import esteticaturnos.model.HorarioLaboral;
import esteticaturnos.model.Profesional;
import esteticaturnos.model.Turno;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

public class FormTurno extends JDialog {

    private static final Color COLOR_ALERTA = new Color(161, 42, 42);

    private final TurnoController turnoController = new TurnoController();
    private final ProfesionalController profesionalController = new ProfesionalController();
    private final Turno turno;
    private boolean guardado = false;

    private final JComboBox<Profesional> cboProfesional = new JComboBox<>();
    private final JComboBox<String> cboServicio = new JComboBox<>();
    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<LocalTime> cboHora = new JComboBox<>();
    private final JLabel lblDisponibilidad = new JLabel(" ");
    private final JTextField txtDetalle = new JTextField(25);

    public FormTurno(Window padre, Clienta clienta, Turno turnoAEditar) {
        super(padre, turnoAEditar == null ? "Nuevo turno" : "Modificar turno", ModalityType.APPLICATION_MODAL);
        this.turno = turnoAEditar == null ? new Turno() : turnoAEditar;
        this.turno.setClienta(clienta);
        construirPantalla(clienta);
        cargarDatos();
        cboProfesional.addActionListener(e -> alCambiarProfesional());
        spnFecha.addChangeListener(e -> cargarHorasDisponibles());
        pack();
        setLocationRelativeTo(padre);
    }

    public boolean isGuardado() {
        return guardado;
    }

    // ---------- Construcción de la pantalla ----------

    private void construirPantalla(Clienta clienta) {
        setLayout(new BorderLayout());
        add(Estilo.crearEncabezado(getTitle() + " — " + clienta.getNombre()), BorderLayout.NORTH);
        add(crearCampos(clienta), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    private JPanel crearCampos(Clienta clienta) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));
        lblDisponibilidad.setForeground(COLOR_ALERTA);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(panel, c, 0, "Profesional *", cboProfesional);
        agregarFila(panel, c, 1, "Servicio *", cboServicio);
        agregarFila(panel, c, 2, "Fecha *", spnFecha);
        agregarFila(panel, c, 3, "Hora *", cboHora);
        agregarFila(panel, c, 4, "", lblDisponibilidad);
        agregarFila(panel, c, 5, "Detalle", txtDetalle);
        if (clienta.getObservaciones() != null && !clienta.getObservaciones().isBlank()) {
            JLabel lblObservaciones = new JLabel("Atención: " + clienta.getObservaciones());
            lblObservaciones.setForeground(COLOR_ALERTA);
            agregarFila(panel, c, 6, "", lblObservaciones);
        }
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
        JButton btnGuardar = new JButton("Guardar turno");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        getRootPane().setDefaultButton(btnGuardar);
        panel.add(btnCancelar);
        panel.add(btnGuardar);
        return panel;
    }

    // ---------- Carga de datos ----------

    private void cargarDatos() {
        try {
            cboProfesional.setModel(new DefaultComboBoxModel<>(
                    profesionalController.listar().toArray(new Profesional[0])));
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
        if (turno.getId() == 0) {
            spnFecha.setValue(aDate(primerDiaConTurnosPosibles()));
        } else {
            seleccionarProfesional(turno.getProfesional());
            spnFecha.setValue(aDate(turno.getFecha()));
            txtDetalle.setText(turno.getDetalle());
        }
        alCambiarProfesional();
        if (turno.getId() != 0) {
            cboServicio.setSelectedItem(turno.getServicio());
            cboHora.setSelectedItem(turno.getHora());
        }
    }

    private void seleccionarProfesional(Profesional buscada) {
        for (int i = 0; i < cboProfesional.getItemCount(); i++) {
            if (cboProfesional.getItemAt(i).getId() == buscada.getId()) {
                cboProfesional.setSelectedIndex(i);
                return;
            }
        }
    }

    private void alCambiarProfesional() {
        Profesional profesional = profesionalSeleccionada();
        String[] servicios = profesional == null ? new String[0] : profesional.getRubro().getServicios();
        cboServicio.setModel(new DefaultComboBoxModel<>(servicios));
        cargarHorasDisponibles();
    }

    private void cargarHorasDisponibles() {
        LocalDate fecha = fechaSeleccionada();
        try {
            List<LocalTime> horas = turnoController.horasDisponibles(fecha, profesionalSeleccionada(), turno.getId());
            cboHora.setModel(new DefaultComboBoxModel<>(horas.toArray(new LocalTime[0])));
            lblDisponibilidad.setText(mensajeDeDisponibilidad(fecha, horas));
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private String mensajeDeDisponibilidad(LocalDate fecha, List<LocalTime> horas) {
        if (!HorarioLaboral.esDiaHabil(fecha)) {
            return "Ese día el estudio está cerrado (solo lunes a viernes).";
        }
        if (horas.isEmpty()) {
            return "La profesional no tiene horarios libres ese día.";
        }
        return " ";
    }

    // ---------- Guardar ----------

    private void guardar() {
        turno.setProfesional(profesionalSeleccionada());
        turno.setServicio((String) cboServicio.getSelectedItem());
        turno.setFecha(fechaSeleccionada());
        turno.setHora((LocalTime) cboHora.getSelectedItem());
        turno.setDetalle(txtDetalle.getText().isBlank() ? null : txtDetalle.getText().trim());
        try {
            turnoController.guardar(turno);
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    // ---------- Utilidades ----------

    private Profesional profesionalSeleccionada() {
        return (Profesional) cboProfesional.getSelectedItem();
    }

    private LocalDate fechaSeleccionada() {
        return ((Date) spnFecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate primerDiaConTurnosPosibles() {
        boolean quedanTurnosHoy = LocalTime.now().isBefore(HorarioLaboral.ULTIMO_TURNO);
        LocalDate fecha = quedanTurnosHoy ? LocalDate.now() : LocalDate.now().plusDays(1);
        while (!HorarioLaboral.esDiaHabil(fecha)) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}