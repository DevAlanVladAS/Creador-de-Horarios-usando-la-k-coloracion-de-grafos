package src;
/**
 * Adapter de persistencia para proyectos completos.
 * Permite guardar y cargar el mismo tipo de datos que se exportan.
 */
public interface PersistenciaDeDatos {
    void guardarProyecto(ProyectoDatos proyecto);
    ProyectoDatos cargarProyecto();
}
