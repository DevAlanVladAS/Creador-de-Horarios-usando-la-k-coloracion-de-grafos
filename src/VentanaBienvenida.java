package src;

import javax.swing.*;
import java.awt.*;

/**
 * VentanaBienvenida: Ventana modal que explica el funcionamiento del creador de horarios.
 */
public class VentanaBienvenida extends JDialog {
    
    public VentanaBienvenida(JFrame parent) {
        super(parent, "Bienvenido al Creador de Horarios", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(700, 500));
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 245, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("¿Cómo funciona el Creador de Horarios?");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(60, 90, 154));
        mainPanel.add(lblTitulo, BorderLayout.NORTH);
        
        // Contenido con instrucciones
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(240, 245, 250));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] pasos = {
            "1. CREAR GRUPOS PRIMERO",
            "   • Haz clic en '+ Nuevo Grupo' para crear los grupos de estudiantes",
            "   • Ejemplo: 1A, 1B, 2A, etc.",
            "   • Cada grupo representa una sección de estudiantes",
            "",
            "2. AGREGAR PROFESORES",
            "   • Haz clic en 'Agregar Profesor' para añadir profesores al sistema",
            "   • Completa: nombre, materia asignada",
            "   • Define disponibilidad: días y horas en que puede enseñar",
            "",
            "3. ASIGNAR PROFESORES A GRUPOS",
            "   • Al guardar un profesor, selecciona los grupos donde enseñará",
            "   • El sistema evitará conflictos de horarios",
            "",
            "4. GENERAR HORARIO",
            "   • Una vez tengas grupos y profesores asignados",
            "   • Haz clic en '🔄 Generar Horario' para crear el horario",
            "   • El algoritmo de K-coloración optimizará los horarios",
            "",
            "5. EXPORTAR",
            "   • Descarga tu horario en formato PDF o Excel"
        };
        
        for (String paso : pasos) {
            JLabel lbl = new JLabel(paso);
            if (paso.startsWith("   ")) {
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(new Color(80, 80, 80));
            } else if (paso.isEmpty()) {
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 6));
            } else {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(new Color(60, 90, 154));
            }
            panelContenido.add(lbl);
        }
        
        // Scroll para contenido largo
        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBackground(new Color(240, 245, 250));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotones.setBackground(new Color(240, 245, 250));
        
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnContinuar.setPreferredSize(new Dimension(120, 35));
        btnContinuar.addActionListener(e -> dispose());
        
        panelBotones.add(btnContinuar);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
}
