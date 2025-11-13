import java.util.UUID;

public class GrupoEstudiantes {
    private final String id;
    private String nombre; // e.g., "3A" o "Primero B"

    public GrupoEstudiantes(String nombre) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
    }
    
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    
   
}