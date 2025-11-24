package src;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa una materia del catalogo institucional con ID unico y horas sugeridas.
 */
public class Materia implements Serializable {

    private final String id;
    private final String nombre;
    private int horasSugeridas;

    /**
     * Crea una materia generando ID automaticamente.
     */
    public Materia(String nombre, int horasSugeridas) {
        this(null, nombre, horasSugeridas);
    }

    /**
     * Crea una materia con ID opcional y valida nombre/horas.
     */
    public Materia(String id, String nombre, int horasSugeridas) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia no puede ser vacio");
        }
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre.trim();
        this.horasSugeridas = Math.max(1, horasSugeridas);
    }

    /**
     * ID unico de la materia.
     */
    public String getId() {
        return id;
    }

    /**
     * Nombre legible de la materia.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Horas semanales sugeridas.
     */
    public int getHorasSugeridas() {
        return horasSugeridas;
    }

    /**
     * Actualiza horas sugeridas garantizando minimo 1.
     */
    public void setHorasSugeridas(int horasSugeridas) {
        this.horasSugeridas = Math.max(1, horasSugeridas);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
