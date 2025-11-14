import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;


/**
 * InterfazGrafica: Contenedor principal del JFrame.
 * Versión actualizada con JTabbedPane para gestionar múltiples horarios de grupo.
 */
public class InterfazGrafica extends JFrame {
    
    private static final Logger logger = Logger.getLogger(InterfazGrafica.class.getName());

    // --- Componentes ---
    private JButton jButton1; // Botón '+' (Añadir Profesor/Configuración)
    private JButton jButton2; // Botón 'Nuevo Grupo'
    private JButton jButton3; // Botón 'Crear Horario'
    private JPanel jPanel1; // Contenedor lateral y central
    // El JTabbedPane reemplaza el panelContenedorHorario estático
    private JTabbedPane tabbedPanelHorarios; 
    
    // Referencia al catálogo central
    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance(); 

    public InterfazGrafica() {
        setTitle("Creador de Horarios por K-Coloración para Directores");
        initComponents();
        setLocationRelativeTo(null); 
        
        // Inicializar con un grupo si no hay ninguno.
        if (catalogo.getTodosLosGrupos().isEmpty()) {
            iniciarPrimerGrupo();
        } else {
            cargarPestanasDeGrupos();
        }
    }

    private void iniciarPrimerGrupo() {
        GrupoEstudiantes grupoInicial = new GrupoEstudiantes("3A");
        catalogo.addGrupo(grupoInicial);
        agregarPestanaHorario(grupoInicial);
    }
    
    private void cargarPestanasDeGrupos() {
        tabbedPanelHorarios.removeAll();
        for (GrupoEstudiantes grupo : catalogo.getTodosLosGrupos()) {
            agregarPestanaHorario(grupo);
        }
    }

    /** Agrega una nueva pestaña con un PanelHorario para un grupo específico. */
    private void agregarPestanaHorario(GrupoEstudiantes grupo) {
        PanelHorario nuevoHorario = new PanelHorario("Grupo");
        // Nota: En una implementación real, aquí se pasaría el ID del grupo
        // o un objeto HorarioSemana específico para ese grupo.
        tabbedPanelHorarios.addTab(grupo.getNombre(), nuevoHorario);
    }
    
    // --- MÉTODOS DE ACCIÓN ---
    
    private void jButton1ActionPerformed(ActionEvent evt) {
        // Abrir JDialog con PanelConfiguracion
        JDialog dialog = new JDialog(this, "Gestión de Recursos (Profesores, Salones)", true); 
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        PanelConfiguracion panelConfig = new PanelConfiguracion();
        dialog.getContentPane().add(panelConfig);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); 
        
        // No es necesario recuperar datos, ya que PanelConfiguracion los guarda en el Singleton.
    }
    
    private void jButton2ActionPerformed(ActionEvent evt) {
        // Acción de 'Nuevo Grupo'
        String nombreGrupo = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo grupo (e.g., 1B, 3A):", "Nuevo Grupo", JOptionPane.PLAIN_MESSAGE);
        
        if (nombreGrupo != null && !nombreGrupo.trim().isEmpty()) {
            GrupoEstudiantes nuevoGrupo = new GrupoEstudiantes(nombreGrupo.trim());
            catalogo.addGrupo(nuevoGrupo);
            
            agregarPestanaHorario(nuevoGrupo);
            tabbedPanelHorarios.setSelectedIndex(tabbedPanelHorarios.getTabCount() - 1); // Enfocarse en la nueva pestaña
        }
    }

    private void jButton3ActionPerformed(ActionEvent evt) {
        // Acción de 'Crear Horario'
        if (tabbedPanelHorarios.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay grupos creados para generar un horario.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String grupoSeleccionado = tabbedPanelHorarios.getTitleAt(tabbedPanelHorarios.getSelectedIndex());
        
        JOptionPane.showMessageDialog(this, 
            "Ejecutando K-Coloración para el grupo: " + grupoSeleccionado + "...", 
            "Generando Horario", JOptionPane.INFORMATION_MESSAGE);
            
        // Aquí iría la llamada al Controlador de Algoritmos
    }
    
   
    private void initComponents() {

        jPanel1 = new JPanel();
        jButton3 = new JButton("Crear");
        jButton1 = new JButton("Configuración (+)");
        jButton2 = new JButton("Nuevo Grupo");
        
        tabbedPanelHorarios = new JTabbedPane(); // JTabbedPane para horarios de grupo

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout()); 
        setMinimumSize(new java.awt.Dimension(1000, 650)); // Aumentamos el tamaño para la tabla
        
        // Configuración de Listeners
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);

        // --- Layout del Contenedor Lateral (jPanel1) ---
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        
        jPanel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE) // Botón más ancho
                    .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)) // Botón más ancho
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPanelHorarios, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE))
        );
        
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(tabbedPanelHorarios, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        // --- Armar el JFrame final ---
        getContentPane().add(jPanel1, BorderLayout.CENTER); 
        
        // Footer para el botón 'Crear'
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(jButton3);
        getContentPane().add(footer, BorderLayout.SOUTH);
        
        pack();
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, "Error al configurar el Look and Feel.", ex);
        }

        java.awt.EventQueue.invokeLater(() -> new InterfazGrafica().setVisible(true));
    }
}