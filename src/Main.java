package src;

import javax.swing.*;

/**
 * Punto de entrada de la aplicacion Swing: inicializa L&F y abre la ventana principal.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                InterfazGrafica ventanaPrincipal = new InterfazGrafica();
                ventanaPrincipal.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
