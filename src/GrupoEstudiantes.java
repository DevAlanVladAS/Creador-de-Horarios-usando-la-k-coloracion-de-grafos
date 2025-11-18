package src;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class GrupoEstudiantes {
    private final String id;
    private String nombre; // e.g., "3A" o "Primero B"
    // Lista de IDs de profesores asignados a este grupo
    private final List<String> profesorIds = new ArrayList<>();

    public GrupoEstudiantes(String nombre) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
    }
    
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    
    public void addProfesor(String profesorId) {
        if (profesorId == null) return;
        if (!profesorIds.contains(profesorId)) {
            profesorIds.add(profesorId);
        }
    }

    public void removeProfesor(String profesorId) {
        profesorIds.remove(profesorId);
    }

    public List<String> getProfesorIds() {
        return new ArrayList<>(profesorIds);
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
