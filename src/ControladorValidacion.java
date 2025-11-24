package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador que centraliza la validacion de horarios usando ValidadorDeHorarios.
 */
public class ControladorValidacion {

    private ValidadorDeHorarios validador;

    /**
     * Crea el controlador con un validador por defecto.
     */
    public ControladorValidacion() {
        this.validador = new ValidadorDeHorarios();
    }

    /**
     * Valida todas las reglas sobre un horario.
     * @param horario el horario a validar.
     * @return lista de resultados de validacion (errores o advertencias).
     */
    public List<ResultadoValidacion> validarTodo(HorarioSemana horario) {
        System.out.println("Iniciando validacion completa del horario...");
        if (horario == null) {
            List<ResultadoValidacion> resultados = new ArrayList<>();
            resultados.add(new ResultadoValidacion("Error: El horario a validar es nulo.", ResultadoValidacion.Severidad.ERROR));
            return resultados;
        }
        return validador.validar(horario);
    }
}
