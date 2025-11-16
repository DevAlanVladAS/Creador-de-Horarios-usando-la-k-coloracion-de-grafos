package src;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Representa un bloque de clase dentro del horario.
 * Un bloque siempre abarca una materia, un profesor, un salón y un grupo,
 * con un intervalo horario definido mediante LocalTime.
 *
 * Es una hoja dentro del patrón Composite.
 */
class BloqueHorario implements HorarioComponente {

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private String materia;
    private String profesor;
    private String salon;
    private String grupo;

    private String dia; // asignado por HorarioDia o por el asignador

    // Identificador único del bloque para el grafo, serialización, etc.
    private final String id = UUID.randomUUID().toString();

    // IDs de recursos para integrarse con la UI
    private String profesorId;
    private String salonId;
    private String grupoId;

    /**
     * Constructor con nombres 
     */
    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesor, String salon, String grupo) {

        validarIntervalo(horaInicio, horaFin);

        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.materia = materia;
        this.profesor = profesor;
        this.salon = salon;
        this.grupo = grupo;

        this.dia = null;
    }

    /**
     * Constructor alternativo que recibe IDs para integración
     * con catálogos desde la UI.
     */
    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesorId, String salonId, String grupoId, boolean ids) {

        validarIntervalo(horaInicio, horaFin);

        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.materia = materia;

        // nombres nulos, se usan IDs
        this.profesor = null;
        this.salon = null;
        this.grupo = null;

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
    public void agregar(HorarioComponente comp) {
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
    public LocalTime getHoraFin() { return horaFin; }

    public void setHoraInicio(LocalTime horaInicio) {
        validarIntervalo(horaInicio, this.horaFin);
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(LocalTime horaFin) {
        validarIntervalo(this.horaInicio, horaFin);
        this.horaFin = horaFin;
    }

    public Duration getDuracion() {
        return Duration.between(horaInicio, horaFin);
    }

    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }

    public String getProfesor() { return profesor; }
    public void setProfesor(String profesor) { this.profesor = profesor; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }

    public String getId() { return id; }

    public String getProfesorId() { return profesorId; }
    public void setProfesorId(String profesorId) { this.profesorId = profesorId; }

    public String getSalonId() { return salonId; }
    public void setSalonId(String salonId) { this.salonId = salonId; }

    public String getGrupoId() { return grupoId; }
    public void setGrupoId(String grupoId) { this.grupoId = grupoId; }


    /**
     * Comprueba si dos bloques se traslapan en tiempo (independiente del día).
     * Usado por validadores y asignador.
     *
     * @param b2 otro bloque horario
     * @return true si los intervalos se solapan
     */
    public boolean seSolapaCon(BloqueHorario b2) {
        return !(this.horaFin.isBefore(b2.horaInicio) || this.horaInicio.isAfter(b2.horaFin));
    }

    
    private void validarIntervalo(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null)
            throw new IllegalArgumentException("Las horas no pueden ser null");

        if (!fin.isAfter(inicio))
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
    }

}
