package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Profesor {
    private static final int HORAS_POR_DEFECTO = 5;

    private final String id;
    private String nombre;
    private String materiaAsignada;
    
    private List<String> diasDisponibles; 
    
    private List<String> horasDisponibles; 
    private int horasSemanales; // Total de horas a trabajar a la semana

    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles, int horasSemanales) {
        this(null, nombre, materiaAsignada, diasDisponibles, horasDisponibles, horasSemanales);
    }

    public Profesor(String id, String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles, int horasSemanales) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
        this.nombre = nombre;
        this.materiaAsignada = materiaAsignada;
        this.diasDisponibles = diasDisponibles != null ? new ArrayList<>(diasDisponibles) : new ArrayList<>();
        this.horasDisponibles = horasDisponibles != null ? new ArrayList<>(horasDisponibles) : new ArrayList<>();
        this.horasSemanales = horasSemanales > 0 ? horasSemanales : HORAS_POR_DEFECTO;
    }

    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles) {
        this(nombre, materiaAsignada, diasDisponibles, horasDisponibles, HORAS_POR_DEFECTO);
    }

    public Profesor(String nombre, String materiaAsignada) {
        this(nombre, materiaAsignada, null, null, HORAS_POR_DEFECTO);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMateriaAsignada() { return materiaAsignada; }
    public List<String> getDiasDisponibles() { return Collections.unmodifiableList(diasDisponibles); }
    public List<String> getHorasDisponibles() { return Collections.unmodifiableList(horasDisponibles); }
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

    public boolean disponibleEn(String dia) {
        return diasDisponibles.isEmpty() || diasDisponibles.contains(dia);
    }
    
    public boolean disponibleA(String hora) {
        return horasDisponibles.isEmpty() || horasDisponibles.contains(hora);
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Profesor";
    }
}
