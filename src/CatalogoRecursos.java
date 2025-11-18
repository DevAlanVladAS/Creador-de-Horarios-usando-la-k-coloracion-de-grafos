package src;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CatalogoRecursos {
    
    private static volatile CatalogoRecursos instance;
    
    private final Map<String, Profesor> profesores = new ConcurrentHashMap<>();
    private final Map<String, Salon> salones = new ConcurrentHashMap<>();
    private final Map<String, GrupoEstudiantes> grupos = new ConcurrentHashMap<>();
    private final Map<String, BloqueHorario> bloques = new ConcurrentHashMap<>();
    private final Map<String, Materia> materias = new ConcurrentHashMap<>();
    private final Map<String, AsignacionAcademica> asignaciones = new ConcurrentHashMap<>();
    private final Map<String, List<String>> asignacionABloques = new ConcurrentHashMap<>();

    private CatalogoRecursos() {
        inicializarMateriasBase();
    }
    
    private void inicializarMateriasBase() {
        if (!materias.isEmpty()) {
            return;
        }
        List<Materia> base = Arrays.asList(
            new Materia("Matemáticas", 5),
            new Materia("Español", 5),
            new Materia("Inglés", 4),
            new Materia("Física", 3),
            new Materia("Química", 3),
            new Materia("Biología", 3),
            new Materia("Historia de México", 3),
            new Materia("Historia Universal", 3),
            new Materia("Geografía", 2),
            new Materia("Formación Cívica y Ética", 2),
            new Materia("Educación Física", 2),
            new Materia("Artes", 2),
            new Materia("Tecnología", 2),
            new Materia("Matemáticas Aplicadas", 2)
        );
        base.forEach(m -> materias.put(m.getId(), m));
    }
    
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

    // --- PROFESORES ---
    public void addProfesor(Profesor profesor) {
        profesores.put(profesor.getId(), profesor);
    }

    public void removeProfesor(String id) {
        profesores.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getProfesorId()));
    }

    public Profesor obtenerProfesorPorId(String id) {
        if (id == null) return null;
        return profesores.get(id);
    }

    public List<Profesor> getTodosLosProfesores() {
        return new ArrayList<>(profesores.values());
    }
    
    // --- SALONES ---
    public void addSalon(Salon salon) { salones.put(salon.getId(), salon); }

    public void removeSalon(String id) {
        salones.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getSalonId()));
    }

    public Salon obtenerSalonPorId(String id) {
        if (id == null) return null;
        return salones.get(id);
    }

    public List<Salon> getTodosLosSalones() {
        return new ArrayList<>(salones.values());
    }

    // --- GRUPOS ---
    public void addGrupo(GrupoEstudiantes grupo) { grupos.put(grupo.getId(), grupo); }

    public void removeGrupo(String id) {
        grupos.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getGrupoId()));
    }

    public GrupoEstudiantes obtenerGrupoPorId(String id) {
        if (id == null) return null;
        return grupos.get(id);
    }

    public List<GrupoEstudiantes> getTodosLosGrupos() {
        return new ArrayList<>(grupos.values());
    }

    public List<GrupoEstudiantes> getGruposPorGrado(int grado) {
        return grupos.values().stream()
                .filter(g -> g.getGrado() == grado)
                .collect(Collectors.toList());
    }

    // --- MATERIAS ---
    public void addMateria(Materia materia) {
        materias.put(materia.getId(), materia);
    }

    public void removeMateria(String id) {
        materias.remove(id);
    }

    public Materia obtenerMateriaPorId(String id) { return materias.get(id); }

    public List<Materia> getTodasLasMaterias() {
        return new ArrayList<>(materias.values());
    }

    public Optional<Materia> findMateriaByName(String nombre) {
        return materias.values().stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public void actualizarHorasMateria(String materiaId, int horas) {
        Materia materia = materias.get(materiaId);
        if (materia != null) {
            materia.setHorasSugeridas(horas);
        }
    }

    // --- ASIGNACIONES ---
    public AsignacionAcademica addAsignacionAcademica(AsignacionAcademica asignacion) {
        asignaciones.put(asignacion.getId(), asignacion);
        reconstruirBloquesDeAsignacion(asignacion);
        return asignacion;
    }

    public void actualizarAsignacion(AsignacionAcademica asignacionActualizada) {
        removeBloquesPorAsignacion(asignacionActualizada.getId());
        asignaciones.put(asignacionActualizada.getId(), asignacionActualizada);
        reconstruirBloquesDeAsignacion(asignacionActualizada);
    }

    public void removeAsignacion(String id) {
        removeBloquesPorAsignacion(id);
        asignaciones.remove(id);
    }

    public List<AsignacionAcademica> getAsignaciones() {
        return new ArrayList<>(asignaciones.values());
    }

    public List<AsignacionAcademica> getAsignacionesPorGrupo(String grupoId) {
        return asignaciones.values().stream()
                .filter(a -> grupoId != null && grupoId.equals(a.getGrupoId()))
                .collect(Collectors.toList());
    }

    // --- BLOQUES ---
    public void addBloqueHorario(BloqueHorario bloque) { bloques.put(bloque.getId(), bloque); }
    public void removeBloqueHorario(String id) { bloques.remove(id); }
    public BloqueHorario getBloqueHorarioById(String id) {
        if (id == null) return null;
        return bloques.get(id);
    }
    public List<BloqueHorario> getTodosLosBloques() { return new ArrayList<>(bloques.values()); }
    
    public List<BloqueHorario> getBloquesByGrupoId(String grupoId) {
        return bloques.values().stream()
            .filter(b -> grupoId != null && grupoId.equals(b.getGrupoId()))
            .collect(Collectors.toList());
    }

    public List<BloqueHorario> getBloquesByGrupoIds(List<String> grupoIds) {
        if (grupoIds == null || grupoIds.isEmpty()) return new ArrayList<>();
        return bloques.values().stream()
                .filter(b -> b.getGrupoId() != null && grupoIds.contains(b.getGrupoId()))
                .collect(Collectors.toList());
    }

    public List<BloqueHorario> getBloquesByProfesorId(String profesorId) {
        return bloques.values().stream()
            .filter(b -> profesorId != null && profesorId.equals(b.getProfesorId()))
            .collect(Collectors.toList());
    }

    /**
     * Resetea todos los datos del catálogo e inserta nuevamente las materias base.
     */
    public void reset() {
        profesores.clear();
        salones.clear();
        grupos.clear();
        bloques.clear();
        asignacionABloques.clear();
        asignaciones.clear();
        materias.clear();
        inicializarMateriasBase();
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
            .filter(b -> id != null && id.equals(b.getProfesorId()))
            .map(BloqueHorario::getId)
            .collect(Collectors.toList());
        idsARemover.forEach(bloques::remove);
    }

    private void eliminarAsignaciones(Predicate<AsignacionAcademica> predicate) {
        List<String> ids = asignaciones.values().stream()
                .filter(predicate)
                .map(AsignacionAcademica::getId)
                .collect(Collectors.toList());
        ids.forEach(this::removeAsignacion);
    }

    public void reconstruirBloquesDeAsignacion(AsignacionAcademica asignacion) {
        // Primero, eliminar los bloques viejos asociados a esta asignación para evitar duplicados.
        removeBloquesPorAsignacion(asignacion.getId());

        // Ahora, crear los nuevos bloques desde cero.
        Materia materia = materias.get(asignacion.getMateriaId());
        Profesor profesor = profesores.get(asignacion.getProfesorId());
        if (materia == null) {
            throw new IllegalStateException("Materia no encontrada para la asignación");
        }
        
        // Pasar horas disponibles del profesor si existen
        List<BloqueHorario> nuevosBloques;
        if (profesor != null && profesor.getHorasDisponibles() != null && !profesor.getHorasDisponibles().isEmpty()) {
            nuevosBloques = asignacion.construirBloquesRespetandoDisponibilidad(
                    materia.getNombre(), 
                    profesor.getHorasDisponibles()
            );
        } else {
            nuevosBloques = asignacion.construirBloques(materia.getNombre());
        }
        
        nuevosBloques.forEach(this::addBloqueHorario);
        asignacion.registrarBloques(nuevosBloques);
        asignacionABloques.put(asignacion.getId(), asignacion.getBloqueIds());
    }

    private void removeBloquesPorAsignacion(String asignacionId) {
        List<String> ids = asignacionABloques.remove(asignacionId);
        if (ids == null) {
            return;
        }
        ids.forEach(bloques::remove);
    }
}
