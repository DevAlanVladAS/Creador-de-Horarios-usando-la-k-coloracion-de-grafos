package src;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class GrupoEstudiantes {
    private final String id;
    private String nombre; // e.g., "3A" o "Primero B"
    private int grado; // e.g., 1, 2, 3
    // Lista de IDs de profesores asignados a este grupo
    private final List<String> profesorIds = new ArrayList<>();

    public GrupoEstudiantes(String nombre, int grado) {
        this(null, nombre, grado, null);
    }
    
    public GrupoEstudiantes(String id, String nombre, int grado, List<String> profesores) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.grado = grado;
        if (profesores != null) {
            this.profesorIds.addAll(profesores);
        }
    }
    
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getGrado() { return grado; }
    public void setGrado(int grado) { this.grado = grado; }

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
        return grado + "°" + nombre;
    }
}
