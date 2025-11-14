import javax.swing.*;
import java.awt.*;

/**
 * Clase principal de la interfaz gráfica del sistema de horarios.
 * Representa la ventana principal que contiene los paneles de configuración,
 * notificaciones, preferencias, exportación y el panel del horario.
 */
public class InterfazDeUsuarioGUI extends JFrame {

    private ControladorHorariosFacade controladorFacade;
    private PanelHorario panelHorario;
    private PanelConfiguracion panelConfiguracion;
    private PanelNotificaciones panelNotificaciones;
    private PanelExportacion panelExportacion;
    private PanelPreferencias panelPreferencias;

    /** Constructor: inicializa controladores y componentes visuales */
    public InterfazDeUsuarioGUI(ControladorHorariosFacade controladorFacade) {
        this.controladorFacade = controladorFacade;
        setTitle("Sistema de Creación de Horarios Escolares");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        inicializarComponentes();
    }

    /** Inicializa todos los paneles y los agrega al Frame */
    public void inicializarComponentes() {
        panelHorario = new PanelHorario("Grupo");
        panelConfiguracion = new PanelConfiguracion();
        panelNotificaciones = new PanelNotificaciones();
        panelExportacion = new PanelExportacion();
        panelPreferencias = new PanelPreferencias();

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Horario", panelHorario);
        pestañas.addTab("Configuración", panelConfiguracion);
        pestañas.addTab("Preferencias", panelPreferencias);
        pestañas.addTab("Exportación", panelExportacion);

        add(pestañas, BorderLayout.CENTER);
        add(panelNotificaciones, BorderLayout.SOUTH);
    }

    public void mostrarMensaje(String mensaje) {
        panelNotificaciones.mostrarNotificacion(mensaje);
    }

   
}
