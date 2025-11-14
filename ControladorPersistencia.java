/**
 * Controlador responsable de guardar y cargar los horarios desde archivos.
 * Implementa funciones básicas de persistencia.
 */
public class ControladorPersistencia {

    /** Guarda el horario en un archivo (ej. JSON o XML). */
    public void guardar(HorarioSemana horario) {
        System.out.println("Guardando horario...");
        // Aquí iría la lógica real de escritura en archivo
    }

    /** Carga un horario desde un archivo de ruta especificada. */
    public HorarioSemana cargarHorario(String ruta) {
        System.out.println("Cargando horario desde: " + ruta);
        // Aquí iría la lógica real de lectura de archivo
        return new HorarioSemana();
    }
}
