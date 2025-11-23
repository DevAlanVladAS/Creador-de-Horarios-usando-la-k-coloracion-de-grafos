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

        if (a.getGrupoId().equals(b.getGrupoId())) {
            String mensaje = String.format("Mismo grupo (%s)", a.getGrupo());
            // Incluir los IDs de ambos bloques en el resultado.
            var resultado = new ResultadoValidacion(
                mensaje, 
                ResultadoValidacion.Severidad.ERROR,
                List.of(a.getId(), b.getId())
            );
            return List.of(resultado);
        }

        return Collections.emptyList(); // No hay conflicto.
    }
}
