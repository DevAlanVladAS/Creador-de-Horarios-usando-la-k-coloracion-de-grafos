package src;

import java.util.List;

/**
 * Validador para un unico bloque de horario (no compara pares como Validador).
 * Ideal para reglas o preferencias que aplican a un bloque individual.
 */
public interface UnaryValidator {

    /**
     * Valida las propiedades de un bloque concreto.
     * @param bloque bloque a evaluar
     * @return lista de resultados; vacia si pasa la regla
     */
    List<ResultadoValidacion> validar(BloqueHorario bloque);

}
