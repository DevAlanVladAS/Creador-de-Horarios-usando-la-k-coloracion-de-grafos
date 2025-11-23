package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ValidadorPorDia implements Validador {

    private final List<String> diasPermitidos;

    public ValidadorPorDia(List<String> diasPermitidos) {
        this.diasPermitidos = diasPermitidos != null ? diasPermitidos : Collections.emptyList();
    }

    @Override
    public List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto) {
        List<ResultadoValidacion> resultados = new ArrayList<>();
        if (diasPermitidos.isEmpty()) {
            return resultados;
        }

        // Este validador actúa sobre un solo bloque a la vez, pero lo llamamos en un contexto de pares.
        // Validamos 'bloqueA' y podríamos validar 'bloqueB' si la lógica lo requiriera.
        if (bloqueA.getDia() != null && !diasPermitidos.contains(bloqueA.getDia())) {
            String mensaje = String.format(
                "El bloque '%s' está en un día no permitido (%s).",
                bloqueA.getMateria(), bloqueA.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloqueA.getId()));
        }

        if (bloqueB.getDia() != null && !diasPermitidos.contains(bloqueB.getDia())) {
             String mensaje = String.format(
                "El bloque '%s' está en un día no permitido (%s).",
                bloqueB.getMateria(), bloqueB.getDia()
            );
            resultados.add(new ResultadoValidacion(mensaje, ResultadoValidacion.Severidad.WARNING, bloqueB.getId()));
        }

        return resultados;
    }
}
