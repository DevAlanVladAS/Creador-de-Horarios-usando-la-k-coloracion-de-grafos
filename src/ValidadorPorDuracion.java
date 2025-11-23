package src;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidadorPorDuracion implements Validador {

    private final Duration duracionMaxima;

    /**
     * @param duracionMaximaEnMinutos La duración máxima permitida para un bloque, en minutos.
     */
    public ValidadorPorDuracion(int duracionMaxima) {
        this.duracionMaxima = Duration.ofMinutes(duracionMaxima);
    }

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();

        // Este validador revisa la propiedad de un bloque individualmente.
        // Lo aplicamos a ambos bloques que se nos pasan.
        validarBloque(bloqueA, resultados);
        validarBloque(bloqueB, resultados);

        return resultados;
    }

    private void validarBloque(BloqueHorario bloque, List<ResultadoValidacion> resultados) {
        if (bloque == null || bloque.getDuracion() == null) {
            return;
        }

        if (bloque.getDuracion().compareTo(duracionMaxima) > 0) {
            String mensaje = String.format("El bloque '%s' excede la duración máxima permitida de %d minutos.",
                    bloque.getMateria(), duracionMaxima.toMinutes());
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloque.getId()));
        }
    }
}
