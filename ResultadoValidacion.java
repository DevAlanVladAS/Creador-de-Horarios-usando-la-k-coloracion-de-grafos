/**
 * Clase que representa el resultado de una validación.
 */
public class ResultadoValidacion {
    private String mensaje;

    public ResultadoValidacion(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return mensaje;
    }
}
