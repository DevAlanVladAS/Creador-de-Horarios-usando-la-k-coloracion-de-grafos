package src;
import java.io.Serializable;
import java.util.UUID;

/**
 * Representa una materia del catálogo institucional.
 * Mantiene un identificador único para permitir la referencia desde otras entidades.
 */
public class Materia implements Serializable {

    private final String id = UUID.randomUUID().toString();
    private final String nombre;
    private int horasSugeridas;

    public Materia(String nombre, int horasSugeridas) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia no puede ser vacío");
        }
        this.nombre = nombre.trim();
        this.horasSugeridas = Math.max(1, horasSugeridas);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getHorasSugeridas() {
        return horasSugeridas;
    }

    public void setHorasSugeridas(int horasSugeridas) {
        this.horasSugeridas = Math.max(1, horasSugeridas);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
