package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidadorPorSalon implements Validador {

    public ValidadorPorSalon() {
        // Constructor por defecto
    }

    /**
     * Valida si los bloques usan el mismo salón.
     * @return Una lista con un resultado de ERROR si hay conflicto, o una lista vacía si no lo hay.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario a, BloqueHorario b, HorarioSemana contexto) {
        if (a.getSalonId() == null || b.getSalonId() == null) {
            return Collections.emptyList(); // No se puede determinar el conflicto.
        }

        if (!a.getSalonId().equals(b.getSalonId())) {
            return Collections.emptyList();
        }

        // Conflicto solo si coinciden en día y horario.
        if (a.getDia() != null && b.getDia() != null &&
            a.getDia().equalsIgnoreCase(b.getDia()) &&
            seTraslapan(a, b)) {
            String mensaje = String.format("Mismo salón (ID: %s)", a.getSalonId());
            var resultado = new ResultadoValidacion(
                mensaje, 
                ResultadoValidacion.Severidad.ERROR,
                List.of(a.getId(), b.getId())
            );
            return List.of(resultado);
        }

        return Collections.emptyList(); // No hay conflicto.
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
