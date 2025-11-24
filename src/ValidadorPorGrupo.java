package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validador que comprueba si dos bloques de horario entran en conflicto
 * porque están asignados al mismo grupo.
 */
public class ValidadorPorGrupo implements Validador {

    /**
     * Valida si los bloques son para el mismo grupo.
     * @return Una lista con un resultado de ERROR si hay conflicto, o una lista vacía si no lo hay.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario a, BloqueHorario b, HorarioSemana contexto) {
        if (a.getGrupoId() == null || b.getGrupoId() == null) {
            return Collections.emptyList(); // No se puede determinar el conflicto.
        }

        if (!a.getGrupoId().equals(b.getGrupoId())) {
            return Collections.emptyList();
        }

        if (a.getDia() != null && b.getDia() != null &&
            a.getDia().equalsIgnoreCase(b.getDia()) &&
            seTraslapan(a, b)) {
            String mensaje = String.format("Mismo grupo (%s)", a.getGrupo());
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
