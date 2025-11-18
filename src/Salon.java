package src;
import java.util.UUID;

public class Salon {
    private final String id = UUID.randomUUID().toString();
    private String nombre; // e.g., "Aula 101"
    private int capacidad;

    public Salon(String nombre, int capacidad) {
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
