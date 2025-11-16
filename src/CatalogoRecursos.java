package src;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;

public class CatalogoRecursos {
    
    // 1. Instancia Singleton
    private static volatile CatalogoRecursos instance;
    
    // 2. Colecciones (Usando Map para un acceso rápido por ID)
    private final Map<String, Profesor> profesores = new ConcurrentHashMap<>();
    private final Map<String, Salon> salones = new ConcurrentHashMap<>();
    private final Map<String, GrupoEstudiantes> grupos = new ConcurrentHashMap<>();
    private final Map<String, BloqueHorario> bloques = new ConcurrentHashMap<>();

    // 3. Constructor privado para evitar instanciación externa
    private CatalogoRecursos() {
        // Inicializar con algunos datos de ejemplo si es necesario
        // Pero lo dejaremos vacío para que el director cree todo.
    }
    
    // 4. Método de acceso global
    public static CatalogoRecursos getInstance() {
        if (instance == null) {
            synchronized (CatalogoRecursos.class) {
                if (instance == null) {
                    instance = new CatalogoRecursos();
                }
            }
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

    // --- MÉTODOS DE BLOQUEHORARIO ---
    public void addBloqueHorario(BloqueHorario b) { bloques.put(b.getId(), b); }
    public void removeBloqueHorario(String id) { bloques.remove(id); }
    public BloqueHorario getBloqueHorarioById(String id) { return bloques.get(id); }
    public List<BloqueHorario> getTodosLosBloques() { return new ArrayList<>(bloques.values()); }
    
    public List<BloqueHorario> getBloquesByGrupoId(String grupoId) {
        return bloques.values().stream()
            .filter(b -> b.getGrupoId().equals(grupoId))
            .collect(Collectors.toList());
    }

    public List<BloqueHorario> getBloquesByProfesorId(String profesorId) {
        return bloques.values().stream()
            .filter(b -> b.getProfesorId().equals(profesorId))
            .collect(Collectors.toList());
    }

    /**
     * Resetea todos los datos del catálogo.
     */
    public void reset() {
        profesores.clear();
        salones.clear();
        grupos.clear();
        bloques.clear();
    }

    public Optional<Profesor> findProfesorByName(String nombre) {
        return profesores.values().stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public Optional<GrupoEstudiantes> findGrupoByName(String nombre) {
        return grupos.values().stream()
                .filter(g -> g.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public Optional<Salon> findSalonByName(String nombre) {
        return salones.values().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public void removeBloquesByProfesorId(String id) {
        List<String> idsARemover = bloques.values().stream()
            .filter(b -> id.equals(b.getProfesorId()))
            .map(BloqueHorario::getId)
            .collect(Collectors.toList());
        idsARemover.forEach(bloques::remove);
    }
}