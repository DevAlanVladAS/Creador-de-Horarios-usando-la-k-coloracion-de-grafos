import java.time.LocalTime;

public class ValidadorPorHora implements Validador {
    private LocalTime horaInicioPermitida;
    private LocalTime horaFinPermitida;

    public ValidadorPorHora(LocalTime horaInicioPermitida, LocalTime horaFinPermitida) {
        this.horaInicioPermitida = horaInicioPermitida;
        this.horaFinPermitida = horaFinPermitida;
    }

    @Override
    public boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        return bloqueA.getHoraInicio().compareTo(horaInicioPermitida) >= 0 &&
               bloqueA.getHoraFin().compareTo(horaFinPermitida) <= 0 &&
               bloqueB.getHoraInicio().compareTo(horaInicioPermitida) >= 0 &&
               bloqueB.getHoraFin().compareTo(horaFinPermitida) <= 0;
    }

    @Override
    public String getTipoConflicto(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        return "Conflicto de horario fuera del rango permitido: " +
               horaInicioPermitida + " - " + horaFinPermitida;
    }
}
