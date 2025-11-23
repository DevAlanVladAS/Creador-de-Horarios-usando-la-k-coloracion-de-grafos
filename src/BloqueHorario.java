package src;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Representa un bloque de clase dentro del horario.
 * Un bloque siempre abarca una materia, un profesor, un salón y un grupo,
 * con un intervalo horario definido mediante LocalTime.
 *
 * Es una hoja dentro del patrón Composite.
 * Ahora también implementa el patrón Observer para notificar cambios.
 */
public class BloqueHorario implements HorarioComponente, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private String materia;
    private String profesor;
    private String salon;
    private String grupo;

    private String dia; // asignado por HorarioDia o por el asignador

    // Identificador único del bloque para el grafo, serialización, etc.
    private final String id;

    // IDs de recursos para integrarse con la UI
    private String profesorId;
    private String salonId;
    private String grupoId;

    private transient List<BloqueChangeListener> listeners;

    /**
     * Interface para escuchar cambios en el bloque.
     */
    public interface BloqueChangeListener {
        /**
         * Se invoca cuando alguna propiedad del bloque cambia.
         * @param bloque El bloque que cambió
         * @param propiedad Nombre de la propiedad que cambió
         * @param valorAnterior Valor anterior (puede ser null)
         * @param valorNuevo Valor nuevo (puede ser null)
         */
        void onBloqueChanged(BloqueHorario bloque, String propiedad, 
                           Object valorAnterior, Object valorNuevo);
    }

    private List<BloqueChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        return listeners;
    }

    public void addListener(BloqueChangeListener listener) {
        if (!getListeners().contains(listener)) {
            getListeners().add(listener);
        }
    }

    public void removeListener(BloqueChangeListener listener) {
        getListeners().remove(listener);
    }

    protected void notifyChange(String propiedad, Object valorAnterior, Object valorNuevo) {
        List<BloqueChangeListener> listenersCopy = new ArrayList<>(getListeners());
        
        for (BloqueChangeListener listener : listenersCopy) {
            try {
                listener.onBloqueChanged(this, propiedad, valorAnterior, valorNuevo);
            } catch (Exception e) {
                System.err.println("Error notificando cambio en bloque " + id + ": " + e.getMessage());
            }
        }
    }

    /**
     * Constructor con nombres
     */
    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesor, String salon, String grupo) {
        this(null, horaInicio, horaFin, materia, profesor, salon, grupo, null, null, null);
    }

    public BloqueHorario(String id, LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesor, String salon, String grupo) {
        this(id, horaInicio, horaFin, materia, profesor, salon, grupo, null, null, null);
    }

    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesorId, String salonId, String grupoId, boolean ids) {
        this(null, horaInicio, horaFin, materia, null, null, null, profesorId, salonId, grupoId);
    }

    public BloqueHorario(String id, LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesorId, String salonId, String grupoId, boolean ids) {
        this(id, horaInicio, horaFin, materia, null, null, null, profesorId, salonId, grupoId);
    }

    private BloqueHorario(String id, LocalTime horaInicio, LocalTime horaFin, String materia, String profesor, String salon, String grupo, String profesorId, String salonId, String grupoId) {

        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.materia = materia;
        this.profesor = profesor;
        this.salon = salon;
        this.grupo = grupo;
        this.profesorId = profesorId;
        this.salonId = salonId;
        this.grupoId = grupoId;
        this.dia = null;
    }


    @Override
    public void mostrarInfo() {
        String diaStr = (dia == null) ? "(sin asignar)" : dia;
        String prof = (profesor != null) ? profesor : (profesorId != null ? "profId="+profesorId : "(sin profesor)");
        String sal = (salon != null) ? salon : (salonId != null ? "salId="+salonId : "(sin salon)");
        String grp = (grupo != null) ? grupo : (grupoId != null ? "grpId="+grupoId : "(sin grupo)");

        System.out.println("BloqueHorario [" + diaStr + "]: " + materia + " con " + prof +
                " en " + sal + " para el grupo " + grp +
                " de " + horaInicio + " a " + horaFin + " (id=" + id + ")");
    }

    @Override
    public boolean agregar(HorarioComponente comp) {
        throw new UnsupportedOperationException("No se pueden agregar componentes a un BloqueHorario (hoja).");
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        throw new UnsupportedOperationException("No se puede eliminar componentes de un BloqueHorario (hoja).");
    }

    @Override
    public List<BloqueHorario> getBloques() {
        return List.of(this);
    }


    public LocalTime getHoraInicio() { return horaInicio; }
    
    public void setHoraInicio(LocalTime horaInicio) {
        LocalTime anterior = this.horaInicio;
        this.horaInicio = horaInicio;
        notifyChange("horaInicio", anterior, horaInicio);
    }

    public LocalTime getHoraFin() { return horaFin; }
    
    public void setHoraFin(LocalTime horaFin) {
        LocalTime anterior = this.horaFin;
        this.horaFin = horaFin;
        notifyChange("horaFin", anterior, horaFin);
    }

    public void actualizarIntervalo(LocalTime inicio, LocalTime fin) {
        LocalTime inicioAnterior = this.horaInicio;
        LocalTime finAnterior = this.horaFin;
        
        this.horaInicio = inicio;
        this.horaFin = fin;
        
        notifyChange("intervalo", 
            new LocalTime[]{inicioAnterior, finAnterior},
            new LocalTime[]{inicio, fin});
    }

    public void actualizarPosicion(String dia, LocalTime inicio, LocalTime fin) {
        String diaAnterior = this.dia;
        LocalTime inicioAnterior = this.horaInicio;
        LocalTime finAnterior = this.horaFin;
        
        this.dia = dia;
        this.horaInicio = inicio;
        this.horaFin = fin;
        
        notifyChange("posicion", 
            new Object[]{diaAnterior, inicioAnterior, finAnterior},
            new Object[]{dia, inicio, fin});
    }

    public Duration getDuracion() {
        if (horaInicio == null || horaFin == null) return Duration.ZERO;
        return Duration.between(horaInicio, horaFin);
    }

    public String getMateria() { return materia; }
    
    public void setMateria(String materia) {
        String anterior = this.materia;
        this.materia = materia;
        notifyChange("materia", anterior, materia);
    }

    public String getProfesor() { return profesor; }
    
    public void setProfesor(String profesor) {
        String anterior = this.profesor;
        this.profesor = profesor;
        notifyChange("profesor", anterior, profesor);
    }

    public String getSalon() { return salon; }
    
    public void setSalon(String salon) {
        String anterior = this.salon;
        this.salon = salon;
        notifyChange("salon", anterior, salon);
    }

    public String getGrupo() { return grupo; }
    
    public void setGrupo(String grupo) {
        String anterior = this.grupo;
        this.grupo = grupo;
        notifyChange("grupo", anterior, grupo);
    }

    public String getDia() { return dia; }
    
    public void setDia(String dia) {
        String anterior = this.dia;
        this.dia = dia;
        notifyChange("dia", anterior, dia);
    }

    public String getId() { return id; }

    public String getProfesorId() { return profesorId; }
    
    public void setProfesorId(String profesorId) {
        String anterior = this.profesorId;
        this.profesorId = profesorId;
        notifyChange("profesorId", anterior, profesorId);
    }

    public String getSalonId() { return salonId; }
    
    public void setSalonId(String salonId) {
        String anterior = this.salonId;
        this.salonId = salonId;
        notifyChange("salonId", anterior, salonId);
    }

    public String getGrupoId() { return grupoId; }
    
    public void setGrupoId(String grupoId) {
        String anterior = this.grupoId;
        this.grupoId = grupoId;
        notifyChange("grupoId", anterior, grupoId);
    }
}