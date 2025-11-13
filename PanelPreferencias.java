import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Panel de preferencias del usuario. Permite editar y guardar configuraciones personales.
 */
public class PanelPreferencias extends JPanel {

    private Map<String, Object> preferenciasUsuario;
    private ControladorValidacion controladorValidacion;

    public PanelPreferencias() {
        preferenciasUsuario = new HashMap<>();
        setLayout(new GridLayout(2, 1, 10, 10));
        add(new JButton("Editar Preferencias"));
        add(new JButton("Guardar Preferencias"));
    }

    public void editarPreferencias() {
        // Mostrar un cuadro de diálogo para editar las preferencias
    }

    public void guardarPreferencias() {
        // Guardar las preferencias modificadas
    }
}
