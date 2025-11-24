package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Valida que los bloques esten ubicados solo en dias permitidos.
 * Genera advertencias si un bloque cae fuera de la lista configurada.
 */
public class ValidadorPorDia implements Validador {

    private final List<String> diasPermitidos;

    /**
     * Crea el validador con la lista de dias permitidos (puede ser vacia).
     */
    public ValidadorPorDia(List<String> diasPermitidos) {
        this.diasPermitidos = diasPermitidos != null ? diasPermitidos : Collections.emptyList();
    }

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();
        if (diasPermitidos.isEmpty()) {
            return resultados;
        }

        if (bloqueA.getDia() != null && !diasPermitidos.contains(bloqueA.getDia())) {
            String mensaje = String.format(
                "El bloque '%s' esta en un dia no permitido (%s).",
                bloqueA.getMateria(), bloqueA.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloqueA.getId()));
        }

        if (bloqueB.getDia() != null && !diasPermitidos.contains(bloqueB.getDia())) {
             String mensaje = String.format(
                "El bloque '%s' esta en un dia no permitido (%s).",
                bloqueB.getMateria(), bloqueB.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloqueB.getId()));
        }

        return resultados;
    }
}
