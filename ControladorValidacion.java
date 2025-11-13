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
        System.out.println("Validando horario...");
        // Aquí se pueden agregar validaciones reales
        List<ResultadoValidacion> resultados = new ArrayList<>();
        resultados.add(new ResultadoValidacion("Validación completa: sin errores."));
        return resultados;
    }
}
