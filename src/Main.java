package src;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public class Main {
    

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                inicializarAplicacion();
            } catch (Exception e) {
                System.err.println("Error durante la inicialización de la aplicación:");
                e.printStackTrace();
                e.getMessage();
            }
        });
    }
    
    private static void inicializarAplicacion() {
        InterfazGrafica ventanaPrincipal = new InterfazGrafica();
        
        ventanaPrincipal.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        ventanaPrincipal.setVisible(true);
    }
}
