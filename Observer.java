
/**
 * Interfaz del patrón Observer.
 * Cualquier clase que quiera "observar" los cambios del sistema de horarios
 * (por ejemplo, una interfaz gráfica o un controlador visual)
 * debe implementar esta interfaz.
 * 
 * @author Aldo
 */
public interface Observer {

    /**
     * Método que se llama automáticamente cuando el sujeto (observable)
     * notifica un cambio o evento importante.
     * 
     * @param mensaje texto con información sobre lo ocurrido.
     */
    void actualizar(String mensaje);
}
