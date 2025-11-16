package src;
import javax.swing.*;
import java.awt.*;

/**
 * Panel encargado de exportar los horarios a formato PNG o guardar progreso.
 */
public class PanelExportacion extends JPanel {

    private ControladorPersistencia controladorPersistencia;

    public PanelExportacion() {
        setLayout(new GridLayout(2, 1, 10, 10));
        add(new JButton("Exportar a PNG"));
        add(new JButton("Guardar Progreso (JSON)"));
    }

    public void exportarPNG() {
        // Lógica para exportar visualización como imagen
    }

    public void guardarProgresoJSON() {
        // Lógica para guardar el horario en un archivo JSON
    }
}
