package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Valida que un bloque respete las preferencias de dia del profesor (warning si no).
 */
public class ValidadorPorPreferenciaProfesor implements UnaryValidator {

    private final CatalogoRecursos catalogo;

    /**
     * Usa el CatalogoRecursos singleton para consultar disponibilidad.
     */
    public ValidadorPorPreferenciaProfesor() {
        this.catalogo = CatalogoRecursos.getInstance();
    }

    /**
     * Genera advertencia si el bloque se ubica en un dia fuera de la disponibilidad del profesor.
     */
    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloque) {
        if (bloque.getProfesorId() == null || bloque.getDia() == null) {
            return Collections.emptyList();
        }

        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return Collections.emptyList();
        }

        if (!profesor.getDiasDisponibles().isEmpty() && !profesor.disponibleEn(bloque.getDia())) {
            List<ResultadoValidacion> resultados = new ArrayList<>();
            String mensaje = String.format(
                "Preferencia de profesor: %s no tiene disponibilidad el dia %s.",
                profesor.getNombre(),
                bloque.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING));
            return resultados;
        }

        return Collections.emptyList();
    }
}
