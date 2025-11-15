package src;
import java.time.LocalTime;

public class ValidadorPorHora implements Validador {

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {
        if (!a.getDia().equals(b.getDia())) return true;

        LocalTime inicioA = a.getHoraInicio();
        LocalTime finA = a.getHoraFin();
        LocalTime inicioB = b.getHoraInicio();
        LocalTime finB = b.getHoraFin();

        // Traslape: A empieza antes de que termine B Y A termina despues de que empieza B
        boolean seTraslapan = inicioA.isBefore(finB) && finA.isAfter(inicioB);
        return !seTraslapan;
    }

    @Override
    public String getTipoConflicto() {
        return "Conflicto de Horario (traslape de horas)";
    }
}
