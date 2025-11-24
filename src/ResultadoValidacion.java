package src;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de una validacion: mensaje, severidad y bloques involucrados.
 */
public class ResultadoValidacion {

    /**
     * Niveles de severidad: ERROR (conflicto critico) o WARNING (preferencia/incumplimiento leve).
     */
    public enum Severidad {
        ERROR,
        WARNING
    }

    private final String mensaje;
    private final Severidad severidad;
    private final List<String> idsBloquesInvolucrados;

    /**
     * Crea un resultado con lista de bloques involucrados.
     */
    public ResultadoValidacion(String mensaje, Severidad severidad, List<String> idsBloquesInvolucrados) {
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.idsBloquesInvolucrados = idsBloquesInvolucrados != null ? idsBloquesInvolucrados : Collections.emptyList();
    }
    
    /**
     * Crea un resultado para un unico bloque involucrado.
     */
    public ResultadoValidacion(String mensaje, Severidad severidad, String idBloque) {
        this(mensaje, severidad, Collections.singletonList(idBloque));
    }
    
    /**
     * Crea un resultado sin bloques asociados.
     */
    public ResultadoValidacion(String mensaje, Severidad severidad) {
        this(mensaje, severidad, Collections.emptyList());
    }

    /** Mensaje descriptivo. */
    public String getMensaje() {
        return mensaje;
    }

    /** Severidad del resultado (puede ser null para exito). */
    public Severidad getSeveridad() {
        return severidad;
    }

    /** IDs de bloques involucrados en el conflicto/advertencia. */
    public List<String> getIdsBloquesInvolucrados() {
        return idsBloquesInvolucrados;
    }

    @Override
    public String toString() {
        if (severidad != null) {
            return String.format("[%s] %s", severidad, mensaje);
        }
        return mensaje;
    }

    /**
     * Fabrica un resultado de exito/informativo sin severidad ni bloques.
     */
    public static ResultadoValidacion ofSuccess(String mensaje) {
        return new ResultadoValidacion(mensaje, null, Collections.emptyList());
    }
}
