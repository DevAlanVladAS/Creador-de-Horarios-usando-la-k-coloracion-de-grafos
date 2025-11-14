import javax.swing.*;
import java.awt.*;

/**
 * Panel que muestra notificaciones al usuario en tiempo real.
 */
public class PanelNotificaciones extends JPanel {

    private JLabel etiquetaNotificacion;

    public PanelNotificaciones() {
        setLayout(new BorderLayout());
        etiquetaNotificacion = new JLabel("Listo.", SwingConstants.CENTER);
        etiquetaNotificacion.setForeground(Color.BLUE);
        add(etiquetaNotificacion, BorderLayout.CENTER);
    }

    public void mostrarNotificacion(String mensaje) {
        etiquetaNotificacion.setText(mensaje);
    }
}
