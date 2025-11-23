package src;

import java.io.IOException;

/**
 * Adaptador concreto que persiste proyectos en formato JSON.
 * Reutiliza el {@link ControladorPersistencia} para mantener el mismo formato
 * que usa la exportacion desde la interfaz.
 */
public class AdaptadorJSON implements PersistenciaDeDatos {

    private final String archivo;
    private final ControladorPersistencia controlador = new ControladorPersistencia();

    public AdaptadorJSON(String archivo) {
        if (archivo == null || archivo.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        this.archivo = archivo;
    }

    @Override
    public void guardarProyecto(ProyectoDatos proyecto) {
        if (proyecto == null) {
            throw new IllegalArgumentException("No hay proyecto para guardar");
        }
        try {
            controlador.guardarProyecto(proyecto, archivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el proyecto en JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public ProyectoDatos cargarProyecto() {
        try {
            return controlador.cargarProyecto(archivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el proyecto JSON: " + e.getMessage(), e);
        }
    }
}
