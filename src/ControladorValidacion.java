package src;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador encargado de validar horarios según las reglas del sistema.
 * Utiliza un objeto de tipo ValidadorDeHorarios para aplicar las validaciones.
 */
public class ControladorValidacion {

    private ValidadorDeHorarios validador;

    public ControladorValidacion() {
        this.validador = new ValidadorDeHorarios();
    }

    /**
     * Valida todas las reglas sobre un horario.
     * @param horario el horario a validar.
     * @return lista de resultados de validación (errores o advertencias).
     */
    public List<ResultadoValidacion> validarTodo(HorarioSemana horario) {
        System.out.println("Iniciando validación completa del horario...");
        if (horario == null) {
            List<ResultadoValidacion> resultados = new ArrayList<>();
            resultados.add(new ResultadoValidacion("Error: El horario a validar es nulo.", ResultadoValidacion.Severidad.ERROR));
            return resultados;
        }
        return validador.validar(horario);
    }
}
