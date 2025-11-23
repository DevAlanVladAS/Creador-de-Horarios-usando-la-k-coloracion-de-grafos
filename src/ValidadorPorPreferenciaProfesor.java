package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validador que comprueba si un bloque de horario respeta las preferencias de
 * día de un profesor. Este validador genera advertencias (WARNINGS), no errores.
 */
public class ValidadorPorPreferenciaProfesor implements UnaryValidator {

    private final CatalogoRecursos catalogo;

    public ValidadorPorPreferenciaProfesor() {
        this.catalogo = CatalogoRecursos.getInstance();
    }

    /**
     * Valida la preferencia de día del profesor para un bloque de horario.
     * @param bloque El bloque a validar.
     * @return Una lista con una advertencia si no se cumple la preferencia, o una lista vacía.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloque) {
        if (bloque.getProfesorId() == null || bloque.getDia() == null) {
            return Collections.emptyList(); // No se puede validar sin profesor o día.
        }

        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return Collections.emptyList(); // Profesor no encontrado.
        }

        // Si el profesor tiene una lista de días preferidos y el día del bloque no está en ella,
        // se genera una advertencia.
        if (!profesor.getDiasDisponibles().isEmpty() && !profesor.disponibleEn(bloque.getDia())) {
            List<ResultadoValidacion> resultados = new ArrayList<>();
            String mensaje = String.format(
                "Preferencia de profesor: %s no tiene disponibilidad el día %s.",
                profesor.getNombre(),
                bloque.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING));
            return resultados;
        }

        return Collections.emptyList(); // La preferencia se cumple.
    }
}
