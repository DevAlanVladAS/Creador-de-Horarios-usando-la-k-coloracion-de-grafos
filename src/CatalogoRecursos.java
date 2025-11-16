package src;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;

public class CatalogoRecursos {
    
    // 1. Instancia Singleton
    private static CatalogoRecursos instance;
    
    // 2. Colecciones (Usando Map para un acceso rápido por ID)
    private final Map<String, Profesor> profesores = new ConcurrentHashMap<>();
    private final Map<String, Salon> salones = new ConcurrentHashMap<>();
    private final Map<String, GrupoEstudiantes> grupos = new ConcurrentHashMap<>();

    // 3. Constructor privado para evitar instanciación externa
    CatalogoRecursos() {
        // Inicializar con algunos datos de ejemplo si es necesario
        // Pero lo dejaremos vacío para que el director cree todo.
    }
    
    // 4. Método de acceso global
    public static CatalogoRecursos getInstance() {
        if (instance == null) {
            instance = new CatalogoRecursos();
        }
        return instance;
    }

    // --- MÉTODOS DE PROFESOR ---
    public void addProfesor(Profesor prof1) { profesores.put(prof1.getId(), prof1); }
    public void removeProfesor(String id) { profesores.remove(id); }
    public Profesor obtenerProfesorPorId(String id) { return profesores.get(id); }
    public List<Profesor> getTodosLosProfesores() {
        return new ArrayList<>(profesores.values());
    }
    
    // --- MÉTODOS DE SALON ---
    public void addSalon(Salon s) { salones.put(s.getId(), s); }
    public void removeSalon(String id) { salones.remove(id); }
    public Salon obtenerSalonPorId(String id) { return salones.get(id); }
    public List<Salon> getTodosLosSalones() {
        return new ArrayList<>(salones.values());
    }

    // --- MÉTODOS DE GRUPO ---
    public void addGrupo(GrupoEstudiantes g) { grupos.put(g.getId(), g); }
    public void removeGrupo(String id) { grupos.remove(id); }
    public GrupoEstudiantes obtenerGrupoPorId(String id) { return grupos.get(id); }
    public List<GrupoEstudiantes> getTodosLosGrupos() {
        return new ArrayList<>(grupos.values());
    }
    
    /**
     * Resetea todos los datos del catálogo.
     */
    public void reset() {
        profesores.clear();
        salones.clear();
        grupos.clear();
    }

    public Object findProfesorByName(String nombre) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findProfesorByName'");
    }

    public Object findGrupoByName(String nombre) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findGrupoByName'");
    }
}