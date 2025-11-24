package src;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Valida solapes horarias entre dos bloques en el mismo dia.
 */
public class ValidadorPorHora implements Validador {

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();

        if (bloqueA.getDia() == null || bloqueB.getDia() == null || !bloqueA.getDia().equals(bloqueB.getDia())) {
            return resultados;
        }

        if (bloqueA.getHoraInicio() == null || bloqueA.getHoraFin() == null || bloqueB.getHoraInicio() == null || bloqueB.getHoraFin() == null) {
            return resultados;
        }

        LocalTime inicioA = bloqueA.getHoraInicio();
        LocalTime finA = bloqueA.getHoraFin();
        LocalTime inicioB = bloqueB.getHoraInicio();
        LocalTime finB = bloqueB.getHoraFin();

        boolean seTraslapan = inicioA.isBefore(finB) && finA.isAfter(inicioB);

        if (seTraslapan) {
            String descripcion = String.format(
                "Conflicto de horario: El bloque '%s' (%s - %s) se solapa con el bloque '%s' (%s - %s) en el dia %s.",
                bloqueA.getMateria(), inicioA, finA,
                bloqueB.getMateria(), inicioB, finB,
                bloqueA.getDia()
            );
            resultados.add(new ResultadoValidacion(descripcion, ResultadoValidacion.Severidad.ERROR));
        }

        return resultados;
    }
}
