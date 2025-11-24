package src;

import java.util.Collections;
import java.util.List;

/**
 * Valida conflictos de horario entre bloques que usan el mismo salon.
 */
public class ValidadorPorSalon implements Validador {

    /** Constructor por defecto. */
    public ValidadorPorSalon() {
    }

    /**
     * Retorna error si dos bloques en el mismo salon se solapan en dia/hora.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario a, BloqueHorario b, HorarioSemana contexto) {
        if (a.getSalonId() == null || b.getSalonId() == null) {
            return Collections.emptyList();
        }

        if (!a.getSalonId().equals(b.getSalonId())) {
            return Collections.emptyList();
        }

        if (a.getDia() != null && b.getDia() != null &&
            a.getDia().equalsIgnoreCase(b.getDia()) &&
            seTraslapan(a, b)) {
            String mensaje = String.format("Mismo salon (ID: %s)", a.getSalonId());
            var resultado = new ResultadoValidacion(
                mensaje, 
                ResultadoValidacion.Severidad.ERROR,
                List.of(a.getId(), b.getId())
            );
            return List.of(resultado);
        }

        return Collections.emptyList();
    }

    private boolean seTraslapan(BloqueHorario a, BloqueHorario b) {
        if (a.getHoraInicio() == null || a.getHoraFin() == null ||
            b.getHoraInicio() == null || b.getHoraFin() == null) {
            return false;
        }
        return a.getHoraInicio().isBefore(b.getHoraFin()) &&
               b.getHoraInicio().isBefore(a.getHoraFin());
    }
}
