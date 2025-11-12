import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class CatalogoRecursos {
    // Métodos para obtener recursos por id
    public Profesor obtenerProfesorPorId(String id) {
        return profesores.get(id);
    }

    public GrupoEstudiantes obtenerGrupoPorId(String id) {
        return grupos.get(id);
    }

    public Salon obtenerSalonPorId(String id) {
        return salones.get(id);
    }
    private Map<String, Profesor> profesores = new ConcurrentHashMap<>();
    private Map<String, Salon> salones = new ConcurrentHashMap<>();
    private Map<String, GrupoEstudiantes> grupos = new ConcurrentHashMap<>();

    // Profesor
    public void addProfesor(Profesor p) { profesores.put(p.getId(), p); }
    public Profesor getProfesor(String id) { return profesores.get(id); }
    public Optional<Profesor> findProfesorByName(String nombre) {
        return profesores.values().stream().filter(p -> p.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    // Salon
    public void addSalon(Salon s) { salones.put(s.getId(), s); }
    public Salon getSalon(String id) { return salones.get(id); }
    public Optional<Salon> findSalonByName(String nombre) {
        return salones.values().stream().filter(s -> s.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    // GrupoEstudiantes
    public void addGrupo(GrupoEstudiantes g) { grupos.put(g.getId(), g); }
    public GrupoEstudiantes getGrupo(String id) { return grupos.get(id); }
    public Optional<GrupoEstudiantes> findGrupoByName(String nombre) {
        return grupos.values().stream().filter(g -> g.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }
}
