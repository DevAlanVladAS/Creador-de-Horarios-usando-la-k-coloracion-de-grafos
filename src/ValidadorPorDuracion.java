package src;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Valida que la duracion de cada bloque no exceda un maximo establecido.
 */
public class ValidadorPorDuracion implements Validador {

    private final Duration duracionMaxima;

    /**
     * @param duracionMaximaEnMinutos duracion maxima permitida para un bloque (minutos)
     */
    public ValidadorPorDuracion(int duracionMaxima) {
        this.duracionMaxima = Duration.ofMinutes(duracionMaxima);
    }

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();

        // Se evalua cada bloque individualmente
        validarBloque(bloqueA, resultados);
        validarBloque(bloqueB, resultados);

        return resultados;
    }

    /**
     * Evalua un bloque y agrega advertencia si supera la duracion maxima.
     */
    private void validarBloque(BloqueHorario bloque, List<ResultadoValidacion> resultados) {
        if (bloque == null || bloque.getDuracion() == null) {
            return;
        }

        if (bloque.getDuracion().compareTo(duracionMaxima) > 0) {
            String mensaje = String.format("El bloque '%s' excede la duracion maxima permitida de %d minutos.",
                    bloque.getMateria(), duracionMaxima.toMinutes());
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloque.getId()));
        }
    }
}
