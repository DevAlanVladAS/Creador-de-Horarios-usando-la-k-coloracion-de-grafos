package src;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra notificaciones al usuario en tiempo real.
 * Cambia de color según el tipo de mensaje (éxito o error).
 */
public class PanelNotificaciones extends JPanel {

    private JLabel etiquetaNotificacion;
    private static final Color COLOR_EXITO = new Color(34, 139, 34); // ForestGreen
    private static final Color COLOR_ADVERTENCIA = new Color(255, 165, 0); // Orange
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_NEUTRO = Color.BLACK;

    public PanelNotificaciones() {
        setLayout(new BorderLayout());
        etiquetaNotificacion = new JLabel("Listo.", SwingConstants.LEFT);
        etiquetaNotificacion.setForeground(Color.WHITE);
        etiquetaNotificacion.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(etiquetaNotificacion, BorderLayout.CENTER);
        setBackground(COLOR_EXITO);
    }

    /**
     * Muestra una notificación simple con un color neutro.
     * @param mensaje El mensaje a mostrar.
     */
    public void mostrarNotificacion(String mensaje) {
        setBackground(COLOR_NEUTRO);
        etiquetaNotificacion.setText(mensaje);
    }

    /**
     * Procesa y muestra una lista de resultados de validación.
     * @param resultados La lista de resultados proveniente del validador.
     */
    public void mostrarResultados(List<ResultadoValidacion> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            setBackground(COLOR_NEUTRO);
            etiquetaNotificacion.setText("Validación no produjo resultados.");
            return;
        }

        long errores = resultados.stream().filter(r -> r.getSeveridad() == ResultadoValidacion.Severidad.ERROR).count();
        long advertencias = resultados.stream().filter(r -> r.getSeveridad() == ResultadoValidacion.Severidad.WARNING).count();

        if (errores > 0) {
            setBackground(COLOR_ERROR);
            etiquetaNotificacion.setText(String.format("Validación: %d Error(es) y %d Advertencia(s) encontradas.", errores, advertencias));
        } else if (advertencias > 0) {
            setBackground(COLOR_ADVERTENCIA);
            etiquetaNotificacion.setText(String.format("Validación: %d Advertencia(s) encontradas.", advertencias));
        } else {
            setBackground(COLOR_EXITO);
            // Busca el primer resultado que no tenga severidad (mensaje de éxito)
            String mensajeExito = resultados.stream()
                .filter(r -> r.getSeveridad() == null)
                .map(ResultadoValidacion::getMensaje)
                .findFirst()
                .orElse("Validación completada sin conflictos.");
            etiquetaNotificacion.setText(mensajeExito);
        }
    }
}
