package src;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Representa la asignacion academica (profesor, grupo, materia, salon) y
 * calcula los bloques semanales que deben programarse para cumplir las horas.
 */
public class AsignacionAcademica implements Serializable {

    private final String id;
    private final String grupoId;
    private final String profesorId;
    private final String materiaId;
    private final String salonId;
    private int horasSemanales;
    private final List<String> bloqueIds = new ArrayList<>();

    /**
     * Crea una asignacion generando un ID aleatorio.
     */
    public AsignacionAcademica(String grupoId, String profesorId, String materiaId, String salonId, int horasSemanales) {
        this(null, grupoId, profesorId, materiaId, salonId, horasSemanales);
    }

    /**
     * Crea una asignacion con un ID conocido (o generado si es nulo).
     */
    public AsignacionAcademica(String id, String grupoId, String profesorId, String materiaId, String salonId, int horasSemanales) {

        if (grupoId == null || profesorId == null || materiaId == null) {
            throw new IllegalArgumentException("Grupo, profesor y materia son obligatorios");
        }
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.grupoId = grupoId;
        this.profesorId = profesorId;
        this.materiaId = materiaId;
        this.salonId = salonId;
        this.horasSemanales = Math.max(1, horasSemanales);
    }

    /**
     * Identificador de la asignacion.
     */
    public String getId() {
        return id;
    }

    /**
     * ID del grupo al que pertenece esta asignacion.
     */
    public String getGrupoId() {
        return grupoId;
    }

    /**
     * ID del profesor asignado.
     */
    public String getProfesorId() {
        return profesorId;
    }

    /**
     * ID de la materia asignada.
     */
    public String getMateriaId() {
        return materiaId;
    }

    /**
     * ID del salon asignado (puede ser nulo si no se define).
     */
    public String getSalonId() {
        return salonId;
    }

    /**
     * Horas semanales requeridas para esta asignacion.
     */
    public int getHorasSemanales() {
        return horasSemanales;
    }

    /**
     * Ajusta horas semanales garantizando un minimo de 1.
     */
    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = Math.max(1, horasSemanales);
    }

    /**
     * IDs de los bloques generados para esta asignacion.
     */
    public List<String> getBloqueIds() {
        return Collections.unmodifiableList(bloqueIds);
    }

    /**
     * Registra los bloques asociados guardando solo sus IDs.
     */
    public void registrarBloques(List<BloqueHorario> bloques) {
        bloqueIds.clear();
        if (bloques == null) {
            return;
        }
        for (BloqueHorario bloque : bloques) {
            bloqueIds.add(bloque.getId());
        }
    }

    /**
     * Construye bloques usando la plantilla estandar de horas y duracion.
     */
    public List<BloqueHorario> construirBloques(String materiaNombre) {
        return construirBloques(materiaNombre, PlantillaHoraria.BLOQUES_ESTANDAR, PlantillaHoraria.DURACION_BLOQUE);
    }

    /**
     * Construye bloques respetando una lista de horas disponibles (hora exacta de inicio).
     * Si no se proveen horas, recurre al comportamiento estandar.
     */
    public List<BloqueHorario> construirBloquesRespetandoDisponibilidad(String materiaNombre, List<String> horasDisponibles) {
        List<BloqueHorario> bloques = new ArrayList<>();
        if (materiaNombre == null || materiaNombre.isBlank() || horasDisponibles == null || horasDisponibles.isEmpty()) {
            return construirBloques(materiaNombre); // Fallback si no hay horas disponibles
        }

        Duration bloqueDuracion = PlantillaHoraria.DURACION_BLOQUE;
        int bloquesGenerados = 0;

        List<LocalTime> horasDispoLocales = new ArrayList<>();
        for (String hora : horasDisponibles) {
            try {
                String[] partes = hora.split(":");
                int h = Integer.parseInt(partes[0]);
                horasDispoLocales.add(LocalTime.of(h, 0));
            } catch (Exception e) {
                // Ignorar horas mal formateadas
            }
        }

        if (horasDispoLocales.isEmpty()) {
            return construirBloques(materiaNombre); // Fallback
        }

        for (int i = 0; i < horasSemanales && bloquesGenerados < horasSemanales; i++) {
            LocalTime slot = horasDispoLocales.get(i % horasDispoLocales.size());
            int repeticion = i / horasDispoLocales.size();
            LocalTime inicio = slot.plus(bloqueDuracion.multipliedBy(repeticion));
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
            bloquesGenerados++;
        }

        return bloques;
    }

    /**
     * Construye bloques usando una base de horas y una duracion configurables.
     */
    public List<BloqueHorario> construirBloques(String materiaNombre, List<LocalTime> horasBase, Duration duracion) {

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
            LocalTime inicio = slot.plus(bloqueDuracion.multipliedBy(repeticion));
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
