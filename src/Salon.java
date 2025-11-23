package src;
import java.util.UUID;

public class Salon {
    private final String id;
    private String nombre; // e.g., "Aula 101"
    private int capacidad;

    public Salon(String nombre, int capacidad) {
        this(null, nombre, capacidad);
    }

    public Salon(String id, String nombre, int capacidad) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCapacidad() { return capacidad; }
    
    @Override
    public String toString() {
        return nombre;
    }
}
