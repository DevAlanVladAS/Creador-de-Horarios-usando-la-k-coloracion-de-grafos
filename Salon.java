import java.util.UUID;

public class Salon {
    private final String id = UUID.randomUUID().toString();
    private String nombre;

    public Salon(String nombre) {
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
