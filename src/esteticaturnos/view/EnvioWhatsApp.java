package esteticaturnos.view;

import esteticaturnos.model.Profesional;
import esteticaturnos.model.Turno;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class EnvioWhatsApp {

    private static final String PREFIJO_CELULAR_ARGENTINA = "549";
    private static final DateTimeFormatter FORMATO_DIA =
            DateTimeFormatter.ofPattern("EEEE dd/MM", Locale.of("es", "AR"));

    private EnvioWhatsApp() {
    }

    public static String armarMensaje(Profesional profesional, List<Turno> turnos, int dias) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("¡Hola ").append(profesional.getNombre())
               .append("! Estos son tus turnos de los próximos ").append(dias).append(" días:\n");
        LocalDate diaActual = null;
        for (Turno turno : turnos) {
            if (!turno.getFecha().equals(diaActual)) {
                diaActual = turno.getFecha();
                mensaje.append("\n*").append(diaActual.format(FORMATO_DIA)).append("*\n");
            }
            mensaje.append(armarLinea(turno));
        }
        return mensaje.toString();
    }

    private static String armarLinea(Turno turno) {
        StringBuilder linea = new StringBuilder();
        linea.append("- ").append(turno.getHora())
             .append(" | ").append(turno.getClienta().getNombre())
             .append(" | ").append(turno.getServicio());
        if (turno.getDetalle() != null) {
            linea.append(" (").append(turno.getDetalle()).append(")");
        }
        String observaciones = turno.getClienta().getObservaciones();
        if (observaciones != null && !observaciones.isBlank()) {
            linea.append(" | Atención: ").append(observaciones);
        }
        return linea.append("\n").toString();
    }

    public static boolean abrirChat(String telefono, String mensaje) {
        String url = "https://wa.me/" + normalizarTelefono(telefono) + "?text=" + codificar(mensaje);
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            }
        } catch (IOException e) {
            System.out.println("No se pudo abrir el navegador: " + e.getMessage());
        }
        copiarAlPortapapeles(mensaje);
        return false;
    }

    private static String normalizarTelefono(String telefono) {
        String soloNumeros = telefono.replaceAll("\\D", "");
        return soloNumeros.startsWith("54") ? soloNumeros : PREFIJO_CELULAR_ARGENTINA + soloNumeros;
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static void copiarAlPortapapeles(String texto) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
    }
}
