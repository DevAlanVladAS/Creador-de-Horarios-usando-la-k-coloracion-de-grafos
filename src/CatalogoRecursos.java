package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Repositorio en memoria de profesores, salones, grupos, materias,
 * asignaciones y bloques. Implementa acceso singleton y utilidades
 * para crear/eliminar entidades y reconstruir bloques por asignacion.
 */
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
    
    /**
     * Inicializa materias base solo la primera vez.
     */
    private void inicializarMateriasBase() {
        if (!materias.isEmpty()) {
            return;
        }
        List<Materia> base = Arrays.asList(
            new Materia("Matematicas", 5),
            new Materia("Espanol", 5),
            new Materia("Ingles", 4),
            new Materia("Fisica", 3),
            new Materia("Quimica", 3),
            new Materia("Biologia", 3),
            new Materia("Historia de Mexico", 3),
            new Materia("Historia Universal", 3),
            new Materia("Geografia", 2),
            new Materia("Formacion Civica y Etica", 2),
            new Materia("Educacion Fisica", 2),
            new Materia("Artes", 2),
            new Materia("Tecnologia", 2),
            new Materia("Matematicas Aplicadas", 2)
        );
        base.forEach(m -> materias.put(m.getId(), m));
    }
    
    /**
     * Obtiene la instancia unica del catalogo (thread-safe).
     */
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

    /**
     * Agrega o reemplaza un profesor.
     */
    public void addProfesor(Profesor profesor) {
        profesores.put(profesor.getId(), profesor);
    }

    /**
     * Elimina un profesor y limpia asignaciones que lo usen.
     */
    public void removeProfesor(String id) {
        profesores.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getProfesorId()));
    }

    /**
     * Obtiene un profesor por ID.
     */
    public Profesor obtenerProfesorPorId(String id) {
        if (id == null) return null;
        return profesores.get(id);
    }

    /**
     * Lista todos los profesores.
     */
    public List<Profesor> getTodosLosProfesores() {
        return new ArrayList<>(profesores.values());
    }
    
    // --- SALONES ---

    /**
     * Agrega o reemplaza un salon.
     */
    public void addSalon(Salon salon) { salones.put(salon.getId(), salon); }

    /**
     * Elimina un salon y limpia asignaciones que lo usen.
     */
    public void removeSalon(String id) {
        salones.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getSalonId()));
    }

    /**
     * Obtiene un salon por ID.
     */
    public Salon obtenerSalonPorId(String id) {
        if (id == null) return null;
        return salones.get(id);
    }

    /**
     * Lista todos los salones.
     */
    public List<Salon> getTodosLosSalones() {
        return new ArrayList<>(salones.values());
    }

    // --- GRUPOS ---

    /**
     * Agrega o reemplaza un grupo.
     */
    public void addGrupo(GrupoEstudiantes grupo) { grupos.put(grupo.getId(), grupo); }

    /**
     * Elimina un grupo y limpia asignaciones que lo usen.
     */
    public void removeGrupo(String id) {
        grupos.remove(id);
        eliminarAsignaciones(asignacion -> id != null && id.equals(asignacion.getGrupoId()));
    }

    /**
     * Obtiene un grupo por ID.
     */
    public GrupoEstudiantes obtenerGrupoPorId(String id) {
        if (id == null) return null;
        return grupos.get(id);
    }

    /**
     * Lista todos los grupos.
     */
    public List<GrupoEstudiantes> getTodosLosGrupos() {
        return new ArrayList<>(grupos.values());
    }

    /**
     * Lista grupos de un grado especifico.
     */
    public List<GrupoEstudiantes> getGruposPorGrado(int grado) {
        return grupos.values().stream()
                .filter(g -> g.getGrado() == grado)
                .collect(Collectors.toList());
    }

    // --- MATERIAS ---

    /**
     * Agrega o reemplaza una materia.
     */
    public void addMateria(Materia materia) {
        materias.put(materia.getId(), materia);
    }

    /**
     * Elimina una materia por ID.
     */
    public void removeMateria(String id) {
        materias.remove(id);
    }

    /**
     * Obtiene materia por ID.
     */
    public Materia obtenerMateriaPorId(String id) { return materias.get(id); }

    /**
     * Lista todas las materias.
     */
    public List<Materia> getTodasLasMaterias() {
        return new ArrayList<>(materias.values());
    }

    /**
     * Busca materia por nombre (case-insensitive).
     */
    public Optional<Materia> findMateriaByName(String nombre) {
        return materias.values().stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    /**
     * Actualiza las horas sugeridas de una materia.
     */
    public void actualizarHorasMateria(String materiaId, int horas) {
        Materia materia = materias.get(materiaId);
        if (materia != null) {
            materia.setHorasSugeridas(horas);
        }
    }

    // --- ASIGNACIONES ---

    /**
     * Agrega una asignacion y reconstruye sus bloques.
     */
    public AsignacionAcademica addAsignacionAcademica(AsignacionAcademica asignacion) {
        asignaciones.put(asignacion.getId(), asignacion);
        reconstruirBloquesDeAsignacion(asignacion);
        return asignacion;
    }

    /**
     * Actualiza una asignacion y sus bloques asociados.
     */
    public void actualizarAsignacion(AsignacionAcademica asignacionActualizada) {
        removeBloquesPorAsignacion(asignacionActualizada.getId());
        asignaciones.put(asignacionActualizada.getId(), asignacionActualizada);
        reconstruirBloquesDeAsignacion(asignacionActualizada);
    }

    /**
     * Elimina una asignacion y sus bloques.
     */
    public void removeAsignacion(String id) {
        removeBloquesPorAsignacion(id);
        asignaciones.remove(id);
    }

    /**
     * Lista todas las asignaciones.
     */
    public List<AsignacionAcademica> getAsignaciones() {
        return new ArrayList<>(asignaciones.values());
    }

    /**
     * Mapa defensivo de asignacion -> lista de IDs de bloques.
     */
    public Map<String, List<String>> getMapaAsignacionBloques() {
        return asignacionABloques.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new ArrayList<>(e.getValue())
                ));
    }

    /**
     * Lista asignaciones de un grupo.
     */
    public List<AsignacionAcademica> getAsignacionesPorGrupo(String grupoId) {
        return asignaciones.values().stream()
            .filter(a -> grupoId != null && grupoId.equals(a.getGrupoId()))
            .collect(Collectors.toList());
    }

    // --- BLOQUES ---

    /**
     * Agrega o reemplaza un bloque de horario.
     */
    public void addBloqueHorario(BloqueHorario bloque) { bloques.put(bloque.getId(), bloque); }

    /**
     * Elimina un bloque por ID.
     */
    public void removeBloqueHorario(String id) { bloques.remove(id); }

    /**
     * Obtiene un bloque por ID.
     */
    public BloqueHorario getBloqueHorarioById(String id) {
        if (id == null) return null;
        return bloques.get(id);
    }

    /**
     * Lista todos los bloques.
     */
    public List<BloqueHorario> getTodosLosBloques() { return new ArrayList<>(bloques.values()); }
    
    /**
     * Bloques asociados a un grupo.
     */
    public List<BloqueHorario> getBloquesByGrupoId(String grupoId) {
        return bloques.values().stream()
        .filter(b -> grupoId != null && grupoId.equals(b.getGrupoId()))
        .collect(Collectors.toList());
    }

    /**
     * Bloques para varios grupos (union).
     */
    public List<BloqueHorario> getBloquesByGrupoIds(List<String> grupoIds) {
        if (grupoIds == null || grupoIds.isEmpty()) return new ArrayList<>();
        return bloques.values().stream()
            .filter(b -> b.getGrupoId() != null && grupoIds.contains(b.getGrupoId()))
            .collect(Collectors.toList());
    }

    /**
     * Bloques asociados a un profesor.
     */
    public List<BloqueHorario> getBloquesByProfesorId(String profesorId) {
        return bloques.values().stream()
            .filter(b -> profesorId != null && profesorId.equals(b.getProfesorId()))
            .collect(Collectors.toList());
    }

    /**
     * Resetea todo el catalogo y reinstala materias base.
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

    /**
     * Restaura el estado del catalogo desde listas de datos (p. ej. al cargar JSON).
     */
    public void restaurarDesdeDatos(List<Profesor> profesoresDatos,List<Salon> salonesDatos, List<GrupoEstudiantes> gruposDatos, List<Materia> materiasDatos, List<AsignacionAcademica> asignacionesDatos, List<BloqueHorario> bloquesDatos, Map<String, List<String>> asignacionBloquesDatos) {

        profesores.clear();
        salones.clear();
        grupos.clear();
        bloques.clear();
        asignaciones.clear();
        asignacionABloques.clear();
        materias.clear();

        if (profesoresDatos != null) {
            profesoresDatos.forEach(profesor -> profesores.put(profesor.getId(), profesor));
        }
        if (salonesDatos != null) {
            salonesDatos.forEach(salon -> salones.put(salon.getId(), salon));
        }
        if (gruposDatos != null) {
            gruposDatos.forEach(grupo -> grupos.put(grupo.getId(), grupo));
        }

        if (materiasDatos != null && !materiasDatos.isEmpty()) {
            materiasDatos.forEach(materia -> materias.put(materia.getId(), materia));
        } else {
            inicializarMateriasBase();
        }

        if (asignacionesDatos != null) {
            asignacionesDatos.forEach(asignacion -> asignaciones.put(asignacion.getId(), asignacion));
        }

        if (bloquesDatos != null) {
            bloquesDatos.forEach(bloque -> bloques.put(bloque.getId(), bloque));
        }

        if (asignacionBloquesDatos != null) {
            asignacionBloquesDatos.forEach((clave, lista) ->
                asignacionABloques.put(clave, new ArrayList<>(lista)));
        } else if (asignacionesDatos != null) {
            asignacionesDatos.forEach(asignacion ->
                asignacionABloques.put(asignacion.getId(), new ArrayList<>(asignacion.getBloqueIds())));
        }
    }

    /**
     * Busca profesor por nombre.
     */
    public Optional<Profesor> findProfesorByName(String nombre) {
        return profesores.values().stream().filter(p -> p.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    /**
     * Busca grupo por nombre.
     */
    public Optional<GrupoEstudiantes> findGrupoByName(String nombre) {
        return grupos.values().stream().filter(g -> g.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    /**
     * Busca salon por nombre.
     */
    public Optional<Salon> findSalonByName(String nombre) {
        return salones.values().stream().filter(s -> s.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    /**
     * Elimina bloques que usen un profesor (auxiliar de limpieza).
     */
    public void removeBloquesByProfesorId(String id) {
        List<String> idsARemover = bloques.values().stream().filter(b -> id != null && id.equals(b.getProfesorId())).map(BloqueHorario::getId).collect(Collectors.toList());
        idsARemover.forEach(bloques::remove);
    }

    /**
     * Elimina asignaciones que cumplan el predicado.
     */
    private void eliminarAsignaciones(Predicate<AsignacionAcademica> predicate) {
        List<String> ids = asignaciones.values().stream().filter(predicate).map(AsignacionAcademica::getId).collect(Collectors.toList());
        ids.forEach(this::removeAsignacion);
    }

    /**
     * Reconstruye los bloques de una asignacion (limpia antiguos y genera nuevos).
     */
    public void reconstruirBloquesDeAsignacion(AsignacionAcademica asignacion) {
        removeBloquesPorAsignacion(asignacion.getId());

        Materia materia = materias.get(asignacion.getMateriaId());
        Profesor profesor = profesores.get(asignacion.getProfesorId());
        if (materia == null) {
            throw new IllegalStateException("Materia no encontrada para la asignacion");
        }
        
        List<BloqueHorario> nuevosBloques;
        if (profesor != null && profesor.getHorasDisponibles() != null && !profesor.getHorasDisponibles().isEmpty()) {
            nuevosBloques = asignacion.construirBloquesRespetandoDisponibilidad(materia.getNombre(), profesor.getHorasDisponibles());
        } else {
            nuevosBloques = asignacion.construirBloques(materia.getNombre());
        }
        
        nuevosBloques.forEach(this::addBloqueHorario);
        asignacion.registrarBloques(nuevosBloques);
        asignacionABloques.put(asignacion.getId(), asignacion.getBloqueIds());
    }

    /**
     * Elimina bloques asociados a una asignacion.
     */
    private void removeBloquesPorAsignacion(String asignacionId) {
        List<String> ids = asignacionABloques.remove(asignacionId);
        if (ids == null) {
            return;
        }
        ids.forEach(bloques::remove);
    }
}
