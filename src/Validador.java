package src;

import java.util.List;

/**
 * Interfaz para validadores que comprueban la compatibilidad entre dos bloques de horario.
 */
public interface Validador {

    /**
     * Valida si hay conflictos entre dos bloques de horario.
     * @param bloqueA El primer bloque a comparar.
     * @param bloqueB El segundo bloque a comparar.
     * @return Una lista de `ResultadoValidacion`. Si la lista está vacía, no hay conflictos.
     */
    List<ResultadoValidacion> validar(BloqueHorario bloqueA, BloqueHorario bloqueB, HorarioSemana contexto);

}
