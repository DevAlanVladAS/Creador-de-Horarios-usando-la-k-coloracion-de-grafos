package src;
import java.util.List;
import java.util.UUID;

public class Profesor {
    private final String id = UUID.randomUUID().toString();
    private String nombre;
    private String materiaAsignada;
    
    private List<String> diasDisponibles; 
    
    private List<String> horasDisponibles; 
    private int horasSemanales; // Total de horas a trabajar a la semana

    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles, int horasSemanales) {
        this.nombre = nombre;
        this.materiaAsignada = materiaAsignada;
        this.diasDisponibles = diasDisponibles;
        this.horasDisponibles = horasDisponibles;
        this.horasSemanales = horasSemanales;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMateriaAsignada() { return materiaAsignada; }
    public List<String> getDiasDisponibles() { return diasDisponibles; }
    public List<String> getHorasDisponibles() { return horasDisponibles; }
    public int getHorasSemanales() { return horasSemanales; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMateriaAsignada(String materiaAsignada) { this.materiaAsignada = materiaAsignada; }
    public void setDiasDisponibles(List<String> diasDisponibles) { this.diasDisponibles = diasDisponibles; }
    public void setHorasDisponibles(List<String> horasDisponibles) { this.horasDisponibles = horasDisponibles; }
    public void setHorasSemanales(int horasSemanales) { this.horasSemanales = horasSemanales; }

    public boolean disponibleEn(String dia) {
        return diasDisponibles == null || diasDisponibles.isEmpty() || diasDisponibles.contains(dia);
    }
    
    public boolean disponibleA(String hora) {
        return horasDisponibles == null || horasDisponibles.isEmpty() || horasDisponibles.contains(hora);
    }
}