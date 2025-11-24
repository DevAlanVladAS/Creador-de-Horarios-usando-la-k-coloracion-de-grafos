package src;

import java.util.List;

/**
 * Validador que compara dos bloques de horario para detectar conflictos.
 */
public interface Validador {

    /**
     * Valida si hay conflictos entre dos bloques.
     * @param bloqueA primer bloque
     * @param bloqueB segundo bloque
     * @param contexto horario completo (puede usarse para reglas adicionales)
     * @return lista de resultados; vacia si no hay conflictos
     */
    List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto);

}
