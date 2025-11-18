package src;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Describe la carga académica de una materia asignada a un grupo con un profesor.
 * Permite conocer cuántas horas a la semana deben programarse y genera los bloques base.
 */
public class AsignacionAcademica implements Serializable {

    private final String id = UUID.randomUUID().toString();
    private final String grupoId;
    private final String profesorId;
    private final String materiaId;
    private final String salonId;
    private int horasSemanales;
    private final List<String> bloqueIds = new ArrayList<>();

    public AsignacionAcademica(String grupoId,
                               String profesorId,
                               String materiaId,
                               String salonId,
                               int horasSemanales) {

        if (grupoId == null || profesorId == null || materiaId == null) {
            throw new IllegalArgumentException("Grupo, profesor y materia son obligatorios");
        }
        this.grupoId = grupoId;
        this.profesorId = profesorId;
        this.materiaId = materiaId;
        this.salonId = salonId;
        this.horasSemanales = Math.max(1, horasSemanales);
    }

    public String getId() {
        return id;
    }

    public String getGrupoId() {
        return grupoId;
    }

    public String getProfesorId() {
        return profesorId;
    }

    public String getMateriaId() {
        return materiaId;
    }

    public String getSalonId() {
        return salonId;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = Math.max(1, horasSemanales);
    }

    public List<String> getBloqueIds() {
        return Collections.unmodifiableList(bloqueIds);
    }

    public void registrarBloques(List<BloqueHorario> bloques) {
        bloqueIds.clear();
        if (bloques == null) {
            return;
        }
        for (BloqueHorario bloque : bloques) {
            bloqueIds.add(bloque.getId());
        }
    }

    public List<BloqueHorario> construirBloques(String materiaNombre) {
        return construirBloques(materiaNombre, PlantillaHoraria.BLOQUES_ESTANDAR, PlantillaHoraria.DURACION_BLOQUE);
    }

    public List<BloqueHorario> construirBloques(String materiaNombre,
                                                List<LocalTime> horasBase,
                                                Duration duracion) {

        List<BloqueHorario> bloques = new ArrayList<>();
        if (materiaNombre == null || materiaNombre.isBlank()) {
            return bloques;
        }

        List<LocalTime> base = (horasBase == null || horasBase.isEmpty())
                ? PlantillaHoraria.BLOQUES_ESTANDAR
                : horasBase;

        Duration bloqueDuracion = (duracion == null || duracion.isZero() || duracion.isNegative())
                ? PlantillaHoraria.DURACION_BLOQUE
                : duracion;

        for (int i = 0; i < horasSemanales; i++) {
            LocalTime slot = base.get(i % base.size());
            int repeticion = i / base.size();
            LocalTime inicio = slot.plusMinutes(repeticion * 5L);
            LocalTime fin = inicio.plus(bloqueDuracion);

            BloqueHorario bloque = new BloqueHorario(
                    inicio,
                    fin,
                    materiaNombre,
                    profesorId,
                    salonId,
                    grupoId,
                    true
            );
            bloques.add(bloque);
        }

        return bloques;
    }
}
