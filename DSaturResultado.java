import java.util.List;
import java.util.Map;

/**
 * Contenedor del resultado del algoritmo DSATUR.
 * - asignacion: mapa idBloque -> día asignado (solo bloques asignados)
 * - noAsignados: lista de BloqueHorario que no pudieron ser asignados a ningún día válido
 * - horarioSemana: HorarioSemana construido con las asignaciones aplicadas (puede contener
 *                  también bloques sin asignar en su lista de sin asignar)
 */
public class DSaturResultado {
    public final Map<String, String> asignacion;
    public final List<BloqueHorario> noAsignados;
    public final HorarioSemana horarioSemana;

    public DSaturResultado(Map<String, String> asignacion,
                        List<BloqueHorario> noAsignados,
                        HorarioSemana horarioSemana) {
        this.asignacion = asignacion;
        this.noAsignados = noAsignados;
        this.horarioSemana = horarioSemana;
    }
}
