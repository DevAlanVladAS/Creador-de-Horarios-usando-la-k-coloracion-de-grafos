import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

class BloqueHorario implements HorarioComponente {

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String materia;
    private String profesor;
    private String salon;
    private String grupo;

    private String dia;
    // identificador unico del bloque para referencia en los algoritmos y serializacion
    private final String id = UUID.randomUUID().toString();

    // referencias a recursos por id para mantener los campos de texto para compatibilidad con la UI.
    private String profesorId;
    private String salonId;
    private String grupoId;

    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                        String profesor, String salon, String grupo) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.materia = materia;
        this.profesor = profesor;
        this.salon = salon;
        this.grupo = grupo;
        this.dia = null; // por defecto sin asignar
    }

    /**
     * Constructor alternativo que acepta ids de recursos (recomendado para integracion con CatalogoRecursos que se usa desde UI).
     * El boolean 'ids' es solo para distinguir la firma, pasar true cuando se usan ids en lugar de nombres.
     */
    public BloqueHorario(LocalTime horaInicio, LocalTime horaFin, String materia,
                         String profesorId, String salonId, String grupoId, boolean ids) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.materia = materia;
        // Campos de texto quedan null para indicar que se usan ids
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
        throw new UnsupportedOperationException("No se puede agregar a un BloqueHorario");
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        throw new UnsupportedOperationException("No se puede eliminar de un BloqueHorario");
    }

    @Override
    public List<BloqueHorario> getBloques() {
        return List.of(this);
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getSalon() {
        return salon;
    }

    public void setSalon(String salon) {
        this.salon = salon;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    // dia asociado null = sin asignar
    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getId() {
        return id;
    }

    //getters/setters para los ids
    public String getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(String profesorId) {
        this.profesorId = profesorId;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(String grupoId) {
        this.grupoId = grupoId;
    }

}

