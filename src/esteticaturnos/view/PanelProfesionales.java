package esteticaturnos.view;

import esteticaturnos.controller.ProfesionalController;
import esteticaturnos.controller.TurnoController;
import esteticaturnos.controller.ValidacionException;
import esteticaturnos.model.HorarioLaboral;
import esteticaturnos.model.Profesional;
import esteticaturnos.model.Turno;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class PanelProfesionales extends JPanel {

    private static final int DIAS_A_ENVIAR = 7;

    private final ProfesionalController controller = new ProfesionalController();
    private final TurnoController turnoController = new TurnoController();
    private List<Profesional> profesionales = new ArrayList<>();
    private LocalDate diaAgenda = LocalDate.now();

    private final JCheckBox chkMostrarBajas = new JCheckBox("Mostrar dadas de baja");
    private final DefaultTableModel modelo = crearModelo("Nombre y apellido", "Teléfono", "Rubro", "Estado");
    private final DefaultTableModel modeloAgenda = crearModelo("Hora", "Clienta", "Servicio", "Detalle", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JTable tablaAgenda = new JTable(modeloAgenda);
    private final JLabel lblAgenda = new JLabel("Seleccioná una profesional para ver su agenda");

    public PanelProfesionales() {
        setLayout(new BorderLayout());
        Estilo.configurarTabla(tabla);
        Estilo.configurarTabla(tablaAgenda);
        tabla.setDefaultRenderer(Object.class, new RendererInactivos(fila -> !profesionales.get(fila).isActivo()));
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearSeccionProfesionales(), crearSeccionAgenda());
        division.setResizeWeight(0.4);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarAgenda();
            }
        });
        cargarProfesionales();
    }

    // ---------- Construcción de la pantalla ----------

    private JPanel crearSeccionProfesionales() {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chkMostrarBajas.addActionListener(e -> cargarProfesionales());
        filtro.add(chkMostrarBajas);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.add(crearBoton("Nueva profesional", this::nuevaProfesional));
        botones.add(crearBoton("Modificar", this::modificarProfesional));
        botones.add(crearBoton("Dar de baja", this::darDeBaja));
        botones.add(crearBoton("Reactivar", this::reactivar));
        botones.add(crearBoton("Enviar agenda por WhatsApp", this::enviarAgendaPorWhatsApp));

        seccion.add(filtro, BorderLayout.NORTH);
        seccion.add(new JScrollPane(tabla), BorderLayout.CENTER);
        seccion.add(botones, BorderLayout.SOUTH);
        return seccion;
    }

    private JPanel crearSeccionAgenda() {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        lblAgenda.setFont(Estilo.SUBTITULO);

        JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navegacion.add(crearBoton("< Día anterior", () -> cambiarDia(-1)));
        navegacion.add(crearBoton("Hoy", this::irAHoy));
        navegacion.add(crearBoton("Día siguiente >", () -> cambiarDia(1)));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(lblAgenda, BorderLayout.WEST);
        encabezado.add(navegacion, BorderLayout.EAST);

        seccion.add(encabezado, BorderLayout.NORTH);
        seccion.add(new JScrollPane(tablaAgenda), BorderLayout.CENTER);
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

    private void cargarProfesionales() {
        try {
            profesionales = controller.listar(chkMostrarBajas.isSelected());
            modelo.setRowCount(0);
            for (Profesional profesional : profesionales) {
                modelo.addRow(new Object[]{
                    profesional.getNombre(), profesional.getTelefono(), profesional.getRubro(),
                    profesional.isActivo() ? "Activa" : "De baja"});
            }
            cargarAgenda();
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void cargarAgenda() {
        modeloAgenda.setRowCount(0);
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            lblAgenda.setText("Seleccioná una profesional para ver su agenda");
            return;
        }
        Profesional profesional = profesionales.get(fila);
        lblAgenda.setText("Agenda de " + profesional.getNombre() + " — "
                + diaAgenda.format(Estilo.FORMATO_FECHA_CON_DIA));
        if (!HorarioLaboral.esDiaHabil(diaAgenda)) {
            modeloAgenda.addRow(new Object[]{"", "El estudio está cerrado este día", "", "", ""});
            return;
        }
        try {
            List<Turno> turnosDelDia = turnoController.agendaDelDia(profesional, diaAgenda);
            for (LocalTime hora : HorarioLaboral.HORAS) {
                agregarFilaDeAgenda(hora, buscarTurnoDeLaHora(turnosDelDia, hora));
            }
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private Turno buscarTurnoDeLaHora(List<Turno> turnos, LocalTime hora) {
        for (Turno turno : turnos) {
            if (turno.getHora().equals(hora)) {
                return turno;
            }
        }
        return null;
    }

    private void agregarFilaDeAgenda(LocalTime hora, Turno turno) {
        if (turno == null) {
            modeloAgenda.addRow(new Object[]{hora, "— libre —", "", "", ""});
        } else {
            modeloAgenda.addRow(new Object[]{
                hora, turno.getClienta().getNombre(), turno.getServicio(),
                turno.getDetalle() == null ? "" : turno.getDetalle(), turno.getEstado()});
        }
    }

    private void cambiarDia(int dias) {
        diaAgenda = diaAgenda.plusDays(dias);
        cargarAgenda();
    }

    private void irAHoy() {
        diaAgenda = LocalDate.now();
        cargarAgenda();
    }

    // ---------- Acciones de profesionales ----------

    private Profesional profesionalSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Mensajes.aviso(this, "Seleccioná una profesional de la lista.");
            return null;
        }
        return profesionales.get(fila);
    }

    private void nuevaProfesional() {
        FormProfesional formulario = new FormProfesional(SwingUtilities.getWindowAncestor(this), null);
        formulario.setVisible(true);
        if (formulario.isGuardado()) {
            cargarProfesionales();
        }
    }

    private void modificarProfesional() {
        Profesional profesional = profesionalSeleccionada();
        if (profesional == null) {
            return;
        }
        FormProfesional formulario = new FormProfesional(SwingUtilities.getWindowAncestor(this), profesional);
        formulario.setVisible(true);
        cargarProfesionales();
    }

    private void darDeBaja() {
        Profesional profesional = profesionalSeleccionada();
        if (profesional == null || !Mensajes.confirmar(this, "¿Dar de baja a " + profesional.getNombre() + "?")) {
            return;
        }
        try {
            controller.darDeBaja(profesional);
            cargarProfesionales();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void reactivar() {
        Profesional profesional = profesionalSeleccionada();
        if (profesional == null) {
            return;
        }
        try {
            controller.reactivar(profesional);
            cargarProfesionales();
        } catch (ValidacionException e) {
            Mensajes.aviso(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }

    private void enviarAgendaPorWhatsApp() {
        Profesional profesional = profesionalSeleccionada();
        if (profesional == null) {
            return;
        }
        try {
            List<Turno> turnos = turnoController.proximosTurnos(profesional, DIAS_A_ENVIAR);
            if (turnos.isEmpty()) {
                Mensajes.aviso(this, profesional.getNombre() + " no tiene turnos pendientes en los próximos "
                        + DIAS_A_ENVIAR + " días.");
                return;
            }
            String mensaje = EnvioWhatsApp.armarMensaje(profesional, turnos, DIAS_A_ENVIAR);
            if (!EnvioWhatsApp.abrirChat(profesional.getTelefono(), mensaje)) {
                Mensajes.aviso(this, "No se pudo abrir WhatsApp. La agenda se copió al portapapeles:\n"
                        + "pegala en el chat de " + profesional.getNombre() + " con Ctrl + V.");
            }
        } catch (SQLException e) {
            Mensajes.errorDeBase(this, e);
        }
    }
}