package src;

import java.util.Collections;
import java.util.List;

/**
 * Valida conflictos de horario entre bloques impartidos por el mismo profesor.
 */
public class ValidadorPorProfesor implements Validador {

    /** Constructor por defecto. */
    public ValidadorPorProfesor() {
    }

    /**
     * Retorna error si dos bloques del mismo profesor se solapan en dia/hora.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario a, BloqueHorario b, HorarioSemana contexto) {
        if (a.getProfesorId() == null || b.getProfesorId() == null) {
            return Collections.emptyList();
        }

        if (!a.getProfesorId().equals(b.getProfesorId())) {
            return Collections.emptyList();
        }

        if (a.getDia() != null && b.getDia() != null &&
            a.getDia().equalsIgnoreCase(b.getDia()) &&
            seTraslapan(a, b)) {
            String mensaje = String.format("Mismo profesor (ID: %s)", a.getProfesorId());
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
