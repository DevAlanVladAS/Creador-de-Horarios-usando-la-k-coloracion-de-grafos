package src;

import java.util.Collections;
import java.util.List;

/**
 * Clase que representa el resultado de una validación.
 * Incluye un mensaje descriptivo y un nivel de severidad.
 */
public class ResultadoValidacion {

    /**
     * Define el nivel de severidad de un resultado de validación.
     * - ERROR: Conflicto crítico que hace el horario inviable (ej. mismo profesor en dos lugares).
     * - WARNING: Incumplimiento de una regla "suave" o preferencia (ej. profesor fuera de su horario preferido).
     */
    public enum Severidad {
        ERROR,
        WARNING
    }

    private final String mensaje;
    private final Severidad severidad;
    private final List<String> idsBloquesInvolucrados;

    public ResultadoValidacion(String mensaje, Severidad severidad, List<String> idsBloquesInvolucrados) {
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.idsBloquesInvolucrados = idsBloquesInvolucrados != null ? idsBloquesInvolucrados : Collections.emptyList();
    }
    
    public ResultadoValidacion(String mensaje, Severidad severidad, String idBloque) {
        this(mensaje, severidad, Collections.singletonList(idBloque));
    }
    
    public ResultadoValidacion(String mensaje, Severidad severidad) {
        this(mensaje, severidad, Collections.emptyList());
    }

    public String getMensaje() {
        return mensaje;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

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
     * Método de fábrica para crear un resultado de éxito o informativo sin severidad.
     */
    public static ResultadoValidacion ofSuccess(String mensaje) {
        // Llama al constructor que acepta un null explícito para la severidad.
        return new ResultadoValidacion(mensaje, null, Collections.emptyList());
    }
}
