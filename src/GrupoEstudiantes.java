package src;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un grupo de estudiantes (grado y nombre) y los profesores asignados.
 */
public class GrupoEstudiantes {
    private final String id;
    private String nombre;
    private int grado;
    private final List<String> profesorIds = new ArrayList<>();

    /**
     * Crea un grupo generando ID y con lista de profesores vacia.
     */
    public GrupoEstudiantes(String nombre, int grado) {
        this(null, nombre, grado, null);
    }
    
    /**
     * Crea un grupo con ID opcional y lista inicial de profesores.
     */
    public GrupoEstudiantes(String id, String nombre, int grado, List<String> profesores) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.grado = grado;
        if (profesores != null) {
            this.profesorIds.addAll(profesores);
        }
    }
    
    /** ID unico del grupo. */
    public String getId() { return id; }
    /** Nombre descriptivo (ej. "3A"). */
    public String getNombre() { return nombre; }
    /** Grado del grupo (1-3). */
    public int getGrado() { return grado; }
    public void setGrado(int grado) { this.grado = grado; }

    /**
     * Asigna un profesor al grupo (si no estaba ya).
     */
    public void addProfesor(String profesorId) {
        if (profesorId == null) return;
        if (!profesorIds.contains(profesorId)) {
            profesorIds.add(profesorId);
        }
    }

    /**
     * Quita la asignacion de un profesor.
     */
    public void removeProfesor(String profesorId) {
        profesorIds.remove(profesorId);
    }

    /**
     * Lista defensiva de IDs de profesores asignados.
     */
    public List<String> getProfesorIds() {
        return new ArrayList<>(profesorIds);
    }
    
    @Override
    public String toString() {
        return grado + "º" + nombre;
    }
}
