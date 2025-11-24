package src;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra notificaciones en tiempo real y ajusta color segun severidad.
 */
public class PanelNotificaciones extends JPanel {

    private JLabel etiquetaNotificacion;
    private static final Color COLOR_EXITO = new Color(34, 139, 34);
    private static final Color COLOR_ADVERTENCIA = new Color(255, 165, 0);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_NEUTRO = Color.BLACK;

    /**
     * Crea el panel con estilo base.
     */
    public PanelNotificaciones() {
        setLayout(new BorderLayout());
        etiquetaNotificacion = new JLabel("Listo.", SwingConstants.LEFT);
        etiquetaNotificacion.setForeground(Color.WHITE);
        etiquetaNotificacion.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(etiquetaNotificacion, BorderLayout.CENTER);
        setBackground(COLOR_EXITO);
    }

    /**
     * Muestra una notificacion simple en color neutro.
     */
    public void mostrarNotificacion(String mensaje) {
        setBackground(COLOR_NEUTRO);
        etiquetaNotificacion.setText(mensaje);
    }

    /**
     * Procesa y muestra resultados de validacion, coloreando segun severidad.
     */
    public void mostrarResultados(List<ResultadoValidacion> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            setBackground(COLOR_NEUTRO);
            etiquetaNotificacion.setText("Validacion no produjo resultados.");
            return;
        }

        long errores = resultados.stream().filter(r -> r.getSeveridad() == ResultadoValidacion.Severidad.ERROR).count();
        long advertencias = resultados.stream().filter(r -> r.getSeveridad() == ResultadoValidacion.Severidad.WARNING).count();

        if (errores > 0) {
            setBackground(COLOR_ERROR);
            etiquetaNotificacion.setText(String.format("Validacion: %d Error(es) y %d Advertencia(s) encontradas.", errores, advertencias));
        } else if (advertencias > 0) {
            setBackground(COLOR_ADVERTENCIA);
            etiquetaNotificacion.setText(String.format("Validacion: %d Advertencia(s) encontradas.", advertencias));
        } else {
            setBackground(COLOR_EXITO);
            String mensajeExito = resultados.stream()
                .filter(r -> r.getSeveridad() == null)
                .map(ResultadoValidacion::getMensaje)
                .findFirst()
                .orElse("Validacion completada sin conflictos.");
            etiquetaNotificacion.setText(mensajeExito);
        }
    }
}
