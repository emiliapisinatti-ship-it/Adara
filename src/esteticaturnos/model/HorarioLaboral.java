package esteticaturnos.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class HorarioLaboral {

    public static final LocalTime PRIMER_TURNO = LocalTime.of(9, 0);
    public static final LocalTime ULTIMO_TURNO = LocalTime.of(16, 0);
    public static final List<LocalTime> HORAS = generarHoras();

    private HorarioLaboral() {
    }

    private static List<LocalTime> generarHoras() {
        List<LocalTime> horas = new ArrayList<>();
        for (LocalTime hora = PRIMER_TURNO; !hora.isAfter(ULTIMO_TURNO); hora = hora.plusHours(1)) {
            horas.add(hora);
        }
        return List.copyOf(horas);
    }

    public static boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
    }
}
