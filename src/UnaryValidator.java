package src;

import java.util.List;

/**
 * Interfaz para validadores que comprueban las propiedades de un único bloque de horario.
 * A diferencia de `Validador`, no compara dos bloques, sino que analiza uno solo.
 * Es ideal para reglas de negocio o preferencias que aplican a un bloque individualmente.
 */
public interface UnaryValidator {

    /**
     * Valida las propiedades de un único bloque de horario.
     * @param bloque El bloque a validar.
     * @return Una lista de `ResultadoValidacion`. Si la lista está vacía, el bloque es válido según esta regla.
     */
    List<ResultadoValidacion> validar(BloqueHorario bloque);

}
