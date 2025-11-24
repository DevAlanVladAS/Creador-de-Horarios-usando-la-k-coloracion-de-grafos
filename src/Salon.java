package src;
import java.util.UUID;

/**
 * Representa un salon/aula con ID unico, nombre y capacidad.
 */
public class Salon {
    private final String id;
    private String nombre;
    private int capacidad;

    /**
     * Crea un salon generando ID automaticamente.
     */
    public Salon(String nombre, int capacidad) {
        this(null, nombre, capacidad);
    }

    /**
     * Crea un salon con ID opcional.
     */
    public Salon(String id, String nombre, int capacidad) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    /** ID unico del salon. */
    public String getId() { return id; }
    /** Nombre del salon. */
    public String getNombre() { return nombre; }
    /** Capacidad declarada del salon. */
    public int getCapacidad() { return capacidad; }
    
    @Override
    public String toString() {
        return nombre;
    }
}
