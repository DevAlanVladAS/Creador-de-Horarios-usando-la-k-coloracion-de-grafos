package src;

import java.util.HashMap;
import java.util.Map;

/**
 * Modelo sencillo para almacenar los datos generales de un proyecto escolar.
 */
public class ConfiguracionProyecto {

    private String nombreEscuela;
    private final Map<Integer, Integer> gruposPorGrado = new HashMap<>();

    public ConfiguracionProyecto() {
        for (int grado = 1; grado <= 3; grado++) {
            gruposPorGrado.put(grado, 0);
        }
    }

    public String getNombreEscuela() {
        return nombreEscuela;
    }

    public void setNombreEscuela(String nombreEscuela) {
        this.nombreEscuela = nombreEscuela;
    }

    public void setCantidadGrupos(int grado, int cantidad) {
        if (grado < 1 || grado > 3) {
            throw new IllegalArgumentException("El grado debe estar entre 1 y 3");
        }
        gruposPorGrado.put(grado, Math.max(0, cantidad));
    }

    public int getCantidadGrupos(int grado) {
        return gruposPorGrado.getOrDefault(grado, 0);
    }

    public boolean estaCompleta() {
        return nombreEscuela != null && !nombreEscuela.isBlank();
    }

    public void reset() {
        nombreEscuela = null;
        for (int grado = 1; grado <= 3; grado++) {
            gruposPorGrado.put(grado, 0);
        }
    }
}
