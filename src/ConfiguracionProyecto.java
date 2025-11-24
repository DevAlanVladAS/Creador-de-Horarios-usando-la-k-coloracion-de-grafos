package src;

import java.util.HashMap;
import java.util.Map;

/**
 * Modelo sencillo para almacenar los datos generales de un proyecto escolar.
 */
public class ConfiguracionProyecto {

    private String nombreEscuela;
    private final Map<Integer, Integer> gruposPorGrado = new HashMap<>();

    /**
     * Inicializa la configuracion con 0 grupos para grados 1 a 3.
     */
    public ConfiguracionProyecto() {
        for (int grado = 1; grado <= 3; grado++) {
            gruposPorGrado.put(grado, 0);
        }
    }

    /**
     * Nombre de la escuela configurada.
     */
    public String getNombreEscuela() {
        return nombreEscuela;
    }

    /**
     * Define el nombre de la escuela.
     */
    public void setNombreEscuela(String nombreEscuela) {
        this.nombreEscuela = nombreEscuela;
    }

    /**
     * Establece la cantidad de grupos para un grado (1 a 3), sin permitir negativos.
     */
    public void setCantidadGrupos(int grado, int cantidad) {
        if (grado < 1 || grado > 3) {
            throw new IllegalArgumentException("El grado debe estar entre 1 y 3");
        }
        gruposPorGrado.put(grado, Math.max(0, cantidad));
    }

    /**
     * Obtiene la cantidad de grupos registrada para un grado.
     */
    public int getCantidadGrupos(int grado) {
        return gruposPorGrado.getOrDefault(grado, 0);
    }

    /**
     * Indica si la configuracion tiene nombre de escuela definido.
     */
    public boolean estaCompleta() {
        return nombreEscuela != null && !nombreEscuela.isBlank();
    }

    /**
     * Restaura la configuracion a valores iniciales.
     */
    public void reset() {
        nombreEscuela = null;
        for (int grado = 1; grado <= 3; grado++) {
            gruposPorGrado.put(grado, 0);
        }
    }
}
