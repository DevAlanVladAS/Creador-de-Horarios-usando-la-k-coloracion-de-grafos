package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Representa a un profesor: nombre, materia asignada, disponibilidad y carga horaria.
 */
public class Profesor {
    private static final int HORAS_POR_DEFECTO = 5;

    private final String id;
    private String nombre;
    private String materiaAsignada;
    
    private List<String> diasDisponibles; 
    
    private List<String> horasDisponibles; 
    private int horasSemanales;

    /**
     * Crea un profesor con ID generado y datos completos.
     */
    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles, int horasSemanales) {
        this(null, nombre, materiaAsignada, diasDisponibles, horasDisponibles, horasSemanales);
    }

    /**
     * Crea un profesor con ID opcional y listas de disponibilidad.
     */
    public Profesor(String id, String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles, int horasSemanales) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.materiaAsignada = materiaAsignada;
        this.diasDisponibles = diasDisponibles != null ? new ArrayList<>(diasDisponibles) : new ArrayList<>();
        this.horasDisponibles = horasDisponibles != null ? new ArrayList<>(horasDisponibles) : new ArrayList<>();
        this.horasSemanales = horasSemanales > 0 ? horasSemanales : HORAS_POR_DEFECTO;
    }

    /**
     * Crea un profesor con disponibilidad y horas por defecto.
     */
    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles) {
        this(nombre, materiaAsignada, diasDisponibles, horasDisponibles, HORAS_POR_DEFECTO);
    }

    /**
     * Crea un profesor con solo nombre y materia.
     */
    public Profesor(String nombre, String materiaAsignada) {
        this(nombre, materiaAsignada, null, null, HORAS_POR_DEFECTO);
    }

    /** ID unico del profesor. */
    public String getId() { return id; }
    /** Nombre del profesor. */
    public String getNombre() { return nombre; }
    /** Materia asignada al profesor. */
    public String getMateriaAsignada() { return materiaAsignada; }
    /** Dias disponibles (defensivo). */
    public List<String> getDiasDisponibles() { return Collections.unmodifiableList(diasDisponibles); }
    /** Horas disponibles (defensivo). */
    public List<String> getHorasDisponibles() { return Collections.unmodifiableList(horasDisponibles); }
    /** Carga horaria semanal. */
    public int getHorasSemanales() { return horasSemanales; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMateriaAsignada(String materiaAsignada) { this.materiaAsignada = materiaAsignada; }
    public void setDiasDisponibles(List<String> diasDisponibles) {
        this.diasDisponibles = diasDisponibles != null ? new ArrayList<>(diasDisponibles) : new ArrayList<>();
    }
    public void setHorasDisponibles(List<String> horasDisponibles) {
        this.horasDisponibles = horasDisponibles != null ? new ArrayList<>(horasDisponibles) : new ArrayList<>();
    }
    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales > 0 ? horasSemanales : HORAS_POR_DEFECTO;
    }

    /**
     * Indica si el profesor esta disponible el dia indicado.
     */
    public boolean disponibleEn(String dia) {
        return diasDisponibles.isEmpty() || diasDisponibles.contains(dia);
    }
    
    /**
     * Indica si el profesor esta disponible a la hora indicada.
     */
    public boolean disponibleA(String hora) {
        return horasDisponibles.isEmpty() || horasDisponibles.contains(hora);
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Profesor";
    }
}
