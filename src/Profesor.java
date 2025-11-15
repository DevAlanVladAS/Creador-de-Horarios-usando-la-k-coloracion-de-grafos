package src;
import java.util.List;
import java.util.UUID;

public class Profesor {
    private final String id = UUID.randomUUID().toString();
    private String nombre;
    private String materiaAsignada;
    
    private List<String> diasDisponibles; 
    
    private List<String> horasDisponibles; 

    public Profesor(String nombre, String materiaAsignada, List<String> diasDisponibles, List<String> horasDisponibles) {
        this.nombre = nombre;
        this.materiaAsignada = materiaAsignada;
        this.diasDisponibles = diasDisponibles;
        this.horasDisponibles = horasDisponibles;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMateriaAsignada() { return materiaAsignada; }
    public List<String> getDiasDisponibles() { return diasDisponibles; }
    public List<String> getHorasDisponibles() { return horasDisponibles; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMateriaAsignada(String materiaAsignada) { this.materiaAsignada = materiaAsignada; }
    public void setDiasDisponibles(List<String> diasDisponibles) { this.diasDisponibles = diasDisponibles; }
    public void setHorasDisponibles(List<String> horasDisponibles) { this.horasDisponibles = horasDisponibles; }

    public boolean disponibleEn(String dia) {
        return diasDisponibles == null || diasDisponibles.isEmpty() || diasDisponibles.contains(dia);
    }
    
    public boolean disponibleA(String hora) {
        return horasDisponibles == null || horasDisponibles.isEmpty() || horasDisponibles.contains(hora);
    }
}