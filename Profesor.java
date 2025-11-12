import java.util.List;
import java.util.UUID;

public class Profesor {
    private final String id = UUID.randomUUID().toString();
    private String nombre;
    private List<String> diasDisponibles; // e.g., ["Lunes","Martes"]

    public Profesor(String nombre, List<String> diasDisponibles) {
        this.nombre = nombre;
        this.diasDisponibles = diasDisponibles;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public List<String> getDiasDisponibles() { return diasDisponibles; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDiasDisponibles(List<String> diasDisponibles) { this.diasDisponibles = diasDisponibles; }

    public boolean disponibleEn(String dia) {
        return diasDisponibles == null || diasDisponibles.isEmpty() || diasDisponibles.contains(dia);
    }
}
