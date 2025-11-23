package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidadorPorProfesor implements Validador {

    public ValidadorPorProfesor() {
        // Constructor por defecto
    }

    /**
     * Valida si los bloques son impartidos por el mismo profesor.
     * @return Una lista con un resultado de ERROR si hay conflicto, o una lista vacía si no lo hay.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario a, BloqueHorario b, HorarioSemana contexto) {
        if (a.getProfesorId() == null || b.getProfesorId() == null) {
            return Collections.emptyList(); // No se puede determinar el conflicto.
        }

        if (a.getProfesorId().equals(b.getProfesorId())) {
            String mensaje = String.format("Mismo profesor (ID: %s)", a.getProfesorId());
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
