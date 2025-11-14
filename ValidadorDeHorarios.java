import java.util.ArrayList;
import java.util.List;

/**
 * Clase que valida las reglas de los horarios.
 * 
 * Por ahora es un stub (versión básica de prueba).
 */
public class ValidadorDeHorarios {

    /**
     * Ejecuta todas las validaciones sobre el horario dado.
     * 
     * @param horario el horario semanal a validar.
     * @return lista de resultados de validación.
     */
    public List<ResultadoValidacion> validar(HorarioSemana horario) {
        List<ResultadoValidacion> resultados = new ArrayList<>();
        // Ejemplo: simulamos que todo está bien
        resultados.add(new ResultadoValidacion("Validación completada: sin errores detectados."));
        return resultados;
    }
}
