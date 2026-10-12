package esteticaturnos.view;

import esteticaturnos.controller.ClientaController;
import esteticaturnos.controller.TurnoController;
import esteticaturnos.controller.ValidacionException;
import esteticaturnos.model.Clienta;
import esteticaturnos.model.EstadoTurno;
import esteticaturnos.model.Turno;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


public class PanelClientas extends JPanel {

    private final ClientaController clientaController = new ClientaController();
    private final TurnoController turnoController = new TurnoController();

    private List<Clienta> clientas = new ArrayList<>();
    private List<Turno> turnos = new ArrayList<>();

    private final JTextField txtBuscar = new JTextField(20);
    private final JCheckBox chkMostrarBajas = new JCheckBox("Mostrar dadas de baja");
    private final DefaultTableModel modeloClientas =
            crearModelo("Nombre y apellido", "Teléfono", "Observaciones", "Estado");
    private final DefaultTableModel modeloTurnos =
            crearModelo("Fecha", "Hora", "Profesional", "Servicio", "Detalle", "Estado", "Precio");
    private final JTable tablaClientas = new JTable(modeloClientas);
    private final JTable tablaTurnos = new JTable(modeloTurnos);
    private final JLabel lblTurnosDe = new JLabel("Seleccioná una clienta para ver sus turnos");
    private final JLabel lblTotal = new JLabel(" ");

    public PanelClientas() {
        setLayout(new BorderLayout());
        Estilo.configurarTabla(tablaClientas);
        Estilo.configurarTabla(tablaTurnos);
        tablaClientas.setDefaultRenderer(Object.class, new RendererInactivos(fila -> !clientas.get(fila).isActivo()));
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearSeccionClientas(), crearSeccionTurnos());
        division.setResizeWeight(0.5);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
        tablaClientas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarTurnos();
            }
        });
        cargarClientas();
    }

    // ---------- Construcción de la pantalla ----------

    private JPanel crearSeccionClientas() {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarClientas());
        txtBuscar.addActionListener(e -> cargarClientas());
        chkMostrarBajas.addActionListener(e -> cargarClientas());
        busqueda.add(new JLabel("Buscar clienta:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(chkMostrarBajas);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.add(crearBoton("Nueva clienta", this::nuevaClienta));
        botones.add(crearBoton("Modificar", this::modificarClienta));
        botones.add(crearBoton("Dar de baja", this::darDeBajaClienta));
        botones.add(crearBoton("Reactivar", this::reactivarClienta));

        seccion.add(busqueda, BorderLayout.NORTH);
        seccion.add(new JScrollPane(tablaClientas), BorderLayout.CENTER);
        seccion.add(botones, BorderLayout.SOUTH);
        return seccion;
    }

    private JPanel crearSeccionTurnos() {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        lblTurnosDe.setFont(Estilo.SUBTITULO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.add(crearBoton("Nuevo turno", this::nuevoTurno));
        botones.add(crearBoton("Modificar turno", this::modificarTurno));
        botones.add(crearBoton("Marcar realizado", this::marcarRealizado));
        botones.add(crearBoton("Cancelar turno", this::cancelarTurno));

        JPanel pie = new JPanel(new BorderLayout());
        pie.add(botones, BorderLayout.WEST);
        pie.add(lblTotal, BorderLayout.EAST);

        seccion.add(lblTurnosDe, BorderLayout.NORTH);
        seccion.add(new JScrollPane(tablaTurnos), BorderLayout.CENTER);
        seccion.add(pie, BorderLayout.SOUTH);
        return seccion;
    }

    private JButton crearBoton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // ---------- Carga de datos ----------

    private void cargarClientas() {
        try {
            clientas = clientaController.buscar(txtBuscar.getText(), chkMostrarBajas.isSelected());
            modeloClientas.setRowCount(0);
            for (Clienta clienta : clientas) {
                modeloClientas.addRow(new Object[]{
                    clienta.getNombre(), clienta.getTelefono(),
                    clienta.getObservaciones() == null ? "" : clienta.getObservaciones(),
                    clienta.isActivo() ? "Activa" : "De baja"});
            }
            cargarTurnos();
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void cargarTurnos() {
        modeloTurnos.setRowCount(0);
        Clienta clienta = clientaSeleccionada();
        if (clienta == null) {
            turnos = new ArrayList<>();
            lblTurnosDe.setText("Seleccioná una clienta para ver sus turnos");
            lblTotal.setText(" ");
            return;
        }
        try {
            turnos = turnoController.listarPorClienta(clienta.getId());
            for (Turno turno : turnos) {
                modeloTurnos.addRow(new Object[]{
                    turno.getFecha().format(Estilo.FORMATO_FECHA), turno.getHora(),
                    turno.getProfesional().getDescripcion(), turno.getServicio(),
                    turno.getDetalle() == null ? "" : turno.getDetalle(),
                    turno.getEstado(), Estilo.formatearPrecio(turno.getPrecio())});
            }
            lblTurnosDe.setText("Turnos de " + clienta.getNombre());
            lblTotal.setText("Total gastado: " + Estilo.formatearPrecio(turnoController.calcularTotalGastado(turnos)));
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private Clienta clientaSeleccionada() {
        int fila = tablaClientas.getSelectedRow();
        return fila == -1 ? null : clientas.get(fila);
    }

    private Turno turnoSeleccionado() {
        int fila = tablaTurnos.getSelectedRow();
        return fila == -1 ? null : turnos.get(fila);
    }

    // ---------- Acciones de clientas ----------

    private void nuevaClienta() {
        FormClienta formulario = new FormClienta(SwingUtilities.getWindowAncestor(this), null);
        formulario.setVisible(true);
        if (formulario.isGuardado()) {
            cargarClientas();
        }
    }

    private void modificarClienta() {
        Clienta clienta = clientaSeleccionada();
        if (clienta == null) {
            Mensajes.aviso(this, "Seleccioná una clienta de la lista.");
            return;
        }
        FormClienta formulario = new FormClienta(SwingUtilities.getWindowAncestor(this), clienta);
        formulario.setVisible(true);
        cargarClientas();
    }

    private void darDeBajaClienta() {
        Clienta clienta = clientaSeleccionada();
        if (clienta == null) {
            Mensajes.aviso(this, "Seleccioná una clienta de la lista.");
            return;
        }
        if (!clienta.isActivo()) {
            Mensajes.aviso(this, clienta.getNombre() + " ya está dada de baja.");
            return;
        }
        String pregunta = "¿Dar de baja a " + clienta.getNombre() + "?\nSus turnos pendientes se cancelarán.";
        if (!Mensajes.confirmar(this, pregunta)) {
            return;
        }
        try {
            clientaController.darDeBaja(clienta);
            cargarClientas();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void reactivarClienta() {
        Clienta clienta = clientaSeleccionada();
        if (clienta == null) {
            Mensajes.aviso(this, "Seleccioná una clienta de la lista.");
            return;
        }
        try {
            clientaController.reactivar(clienta);
            cargarClientas();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    // ---------- Acciones de turnos ----------

        private void nuevoTurno() {
        Clienta clienta = clientaSeleccionada();
        if (clienta == null) {
            Mensajes.aviso(this, "Seleccioná primero una clienta.");
            return;
        }
        if (!clienta.isActivo()) {
            Mensajes.aviso(this, clienta.getNombre() + " está dada de baja. Reactivala para darle turnos.");
            return;
        }
        abrirFormularioTurno(clienta, null);
    }

    private void modificarTurno() {
        Turno turno = turnoSeleccionado();
        if (turno == null) {
            Mensajes.aviso(this, "Seleccioná un turno de la lista.");
            return;
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            Mensajes.aviso(this, "Solo se pueden modificar turnos pendientes.");
            return;
        }
        abrirFormularioTurno(clientaSeleccionada(), turno);
    }

    private void abrirFormularioTurno(Clienta clienta, Turno turno) {
        FormTurno formulario = new FormTurno(SwingUtilities.getWindowAncestor(this), clienta, turno);
        formulario.setVisible(true);
        cargarTurnos();
    }
    private void marcarRealizado() {
        Turno turno = turnoSeleccionado();
        if (turno == null) {
            Mensajes.aviso(this, "Seleccioná un turno de la lista.");
            return;
        }
        String texto = JOptionPane.showInputDialog(this, "Precio cobrado ($):", "Marcar como realizado",
                JOptionPane.QUESTION_MESSAGE);
        if (texto == null) {
            return;
        }
        try {
            turnoController.marcarRealizado(turno, Double.parseDouble(texto.replace(",", ".").trim()));
            cargarTurnos();
        } catch (NumberFormatException e) {
            Mensajes.aviso(this, "Ingresá un precio válido, por ejemplo 15000.");
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void cancelarTurno() {
        Turno turno = turnoSeleccionado();
        if (turno == null) {
            Mensajes.aviso(this, "Seleccioná un turno de la lista.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Cancelar el turno del " + turno.getFecha().format(Estilo.FORMATO_FECHA)
                + " a las " + turno.getHora() + "?")) {
            return;
        }
        try {
            turnoController.cancelar(turno);
            cargarTurnos();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }
}
