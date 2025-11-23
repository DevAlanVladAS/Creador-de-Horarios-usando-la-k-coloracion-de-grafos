package src;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ValidadorPorHora implements Validador {

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();

        if (bloqueA.getDia() == null || bloqueB.getDia() == null || !bloqueA.getDia().equals(bloqueB.getDia())) {
            return resultados; // No son del mismo día, no hay conflicto de hora.
        }

        if (bloqueA.getHoraInicio() == null || bloqueA.getHoraFin() == null || bloqueB.getHoraInicio() == null || bloqueB.getHoraFin() == null) {
            return resultados; // No se pueden comparar si no tienen horas
        }

        LocalTime inicioA = bloqueA.getHoraInicio();
        LocalTime finA = bloqueA.getHoraFin();
        LocalTime inicioB = bloqueB.getHoraInicio();
        LocalTime finB = bloqueB.getHoraFin();

        // Traslape: A empieza antes de que termine B Y A termina despues de que empieza B
        boolean seTraslapan = inicioA.isBefore(finB) && finA.isAfter(inicioB);

        if (seTraslapan) {
            String descripcion = String.format(
                "Conflicto de horario: El bloque '%s' (%s - %s) se solapa con el bloque '%s' (%s - %s) en el día %s.",
                bloqueA.getMateria(), inicioA, finA,
                bloqueB.getMateria(), inicioB, finB,
                bloqueA.getDia()
            );
            resultados.add(new ResultadoValidacion(descripcion, ResultadoValidacion.Severidad.ERROR));
        }

        return resultados;
    }
}
