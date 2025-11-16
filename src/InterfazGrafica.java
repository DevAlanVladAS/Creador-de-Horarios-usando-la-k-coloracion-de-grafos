
package src;

import java.awt.*;
import javax.swing.*;

/**
 * InterfazGrafica: Contenedor principal del JFrame.
 * Versión moderna con diseño profesional para gestión de horarios.
 */
public class InterfazGrafica extends JFrame {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGrafica frame = new InterfazGrafica();
            frame.setVisible(true);
        });
    }
    // --- Componentes principales ---
    private JTabbedPane tabbedPanelHorarios;
    private JButton btnNuevoGrupo;
    private JButton btnCrearHorario;
    private JButton btnConfiguracion;
    private JButton btnExportar;
    private JButton btnImportar;

    // Panel de estado
    private JLabel lblEstado;

    // Referencia al catálogo central
    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance();

    public InterfazGrafica() {
        setTitle("Creador de Horarios - K-Coloración de Grafos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initComponents();

        // Eliminar cualquier profesor llamado o con materia 'algebra' si existe
        // (petición del usuario)
        catalogo.getTodosLosProfesores().stream()
                .filter(p -> "algebra".equalsIgnoreCase(p.getNombre())
                        || "algebra".equalsIgnoreCase(p.getMateriaAsignada()))
                .forEach(p -> catalogo.removeProfesor(p.getId()));

        // Mostrar placeholder si no hay grupos/profesores aún
        if (catalogo.getTodosLosGrupos().isEmpty()) {
            mostrarPlaceholderCrearHorario();
        } else {
            cargarPestanasDeGrupos();
        }
    }

    private void cargarPestanasDeGrupos() {
        tabbedPanelHorarios.removeAll();
        for (GrupoEstudiantes grupo : catalogo.getTodosLosGrupos()) {
            agregarPestanaHorario(grupo);
        }
    }

    /** Agrega una nueva pestaña con un PanelHorario para un grupo específico. */
    private void agregarPestanaHorario(GrupoEstudiantes grupo) {
        PanelHorario nuevoHorario = new PanelHorario();
        tabbedPanelHorarios.addTab(grupo.getNombre(), nuevoHorario);
    }

    // --- MÉTODOS DE ACCIÓN ---

    private void onConfiguracion() {
        // Abrir diálogo para agregar profesor
        mostrarDialogoAgregarProfesor();
    }

    private void onNuevoGrupo() {
        // Acción de 'Nuevo Grupo'
        String nombreGrupo = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo grupo (e.g., 1B, 3A):",
                "Nuevo Grupo", JOptionPane.PLAIN_MESSAGE);

        if (nombreGrupo != null && !nombreGrupo.trim().isEmpty()) {
            GrupoEstudiantes nuevoGrupo = new GrupoEstudiantes(nombreGrupo.trim());
            catalogo.addGrupo(nuevoGrupo);

            // Reconstruir pestañas desde el catálogo (remueve placeholder si existía)
            cargarPestanasDeGrupos();
            tabbedPanelHorarios.setSelectedIndex(tabbedPanelHorarios.getTabCount() - 1);
        }
    }

    private void onCrearHorario() {
        // Acción de 'Crear Horario'
        if (tabbedPanelHorarios.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay grupos creados para generar un horario.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String grupoSeleccionado = tabbedPanelHorarios.getTitleAt(tabbedPanelHorarios.getSelectedIndex());

        JOptionPane.showMessageDialog(this,
                "Ejecutando K-Coloración para el grupo: " + grupoSeleccionado + "...",
                "Generando Horario", JOptionPane.INFORMATION_MESSAGE);

        lblEstado.setText("Estado: Horario generado para " + grupoSeleccionado);
    }

    private void onExportar() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de exportación en desarrollo.", "Exportar",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onImportar() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de importación en desarrollo.", "Importar",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra un placeholder en la pestaña cuando no hay grupos/profesores. */
    private void mostrarPlaceholderCrearHorario() {
        tabbedPanelHorarios.removeAll();
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Crear horario");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Aún no hay grupos ni profesores. Agrega un profesor y crea un grupo para comenzar.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCrear = new JButton("Crear Grupo");
        btnCrear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrear.addActionListener(e -> onNuevoGrupo());

        JButton btnAgregarProfesor = new JButton("Agregar Profesor");
        btnAgregarProfesor.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregarProfesor.addActionListener(e -> mostrarDialogoAgregarProfesor());

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(btnCrear);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(btnAgregarProfesor);
        panel.add(Box.createVerticalGlue());

        tabbedPanelHorarios.addTab("Inicio", panel);
    }

    /** Diálogo modal para agregar un profesor al catálogo. */
    private void mostrarDialogoAgregarProfesor() {
        // Mostrar el PanelConfiguracion dentro de un diálogo modal (vuelve a la
        // interfaz anterior)
        JDialog dialog = new JDialog(this, "Gestión de Profesores", true);
        PanelConfiguracion panelConfig = new PanelConfiguracion();
        panelConfig.setParentDialog(dialog);
        dialog.getContentPane().add(panelConfig);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void initComponents() {
        // Crear la barra de menú
        JMenuBar menuBar = crearMenuBar();
        setJMenuBar(menuBar);

        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 242, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior con botones
        JPanel topPanel = crearPanelSuperior();

        // Panel central con tabbedPane
        tabbedPanelHorarios = new JTabbedPane(JTabbedPane.TOP);
        tabbedPanelHorarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPanelHorarios.setBackground(new Color(255, 255, 255));

        // Panel inferior con estado
        JPanel bottomPanel = crearPanelInferior();

        // Armar el layout
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPanelHorarios, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(60, 90, 154));
        menuBar.setForeground(Color.WHITE);

        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setForeground(Color.WHITE);

        JMenuItem itemNuevo = new JMenuItem("Nuevo Proyecto");
        JMenuItem itemAbrir = new JMenuItem("Abrir");
        JMenuItem itemGuardar = new JMenuItem("Guardar");
        JMenuItem itemExportar = new JMenuItem("Exportar");
        JMenuItem itemSalir = new JMenuItem("Salir");

        itemSalir.addActionListener(e -> System.exit(0));
        itemExportar.addActionListener(e -> onExportar());

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemExportar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        // Menú Editar
        JMenu menuEditar = new JMenu("Editar");
        menuEditar.setForeground(Color.WHITE);
        menuEditar.add(new JMenuItem("Deshacer"));
        menuEditar.add(new JMenuItem("Rehacer"));

        // Menú Herramientas
        JMenu menuHerramientas = new JMenu("Herramientas");
        menuHerramientas.setForeground(Color.WHITE);
        JMenuItem itemValidar = new JMenuItem("Validar Horario");
        itemValidar.addActionListener(e -> JOptionPane.showMessageDialog(this, "Validando horario..."));
        menuHerramientas.add(itemValidar);

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setForeground(Color.WHITE);
        menuAyuda.add(new JMenuItem("Acerca de"));

        menuBar.add(menuArchivo);
        menuBar.add(menuEditar);
        menuBar.add(menuHerramientas);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuAyuda);

        return menuBar;
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel izquierdo con botones principales
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        botonesPanel.setBackground(new Color(240, 242, 245));

        btnConfiguracion = crearBoton("Agregar Profesor", new Color(100, 150, 200));
        btnNuevoGrupo = crearBoton("+ Nuevo Grupo", new Color(76, 175, 80));
        btnCrearHorario = crearBoton(" Generar Horario", new Color(255, 152, 0));
        btnExportar = crearBoton(" Exportar", new Color(156, 39, 176));
        btnImportar = crearBoton(" Importar", new Color(33, 150, 243));

        btnConfiguracion.addActionListener(e -> onConfiguracion());
        btnNuevoGrupo.addActionListener(e -> onNuevoGrupo());
        btnCrearHorario.addActionListener(e -> onCrearHorario());
        btnExportar.addActionListener(e -> onExportar());
        btnImportar.addActionListener(e -> onImportar());

        botonesPanel.add(btnConfiguracion);
        botonesPanel.add(btnNuevoGrupo);
        botonesPanel.add(btnCrearHorario);
        botonesPanel.add(btnExportar);
        botonesPanel.add(btnImportar);

        panel.add(botonesPanel, BorderLayout.WEST);

        // Información del lado derecho
        JLabel lblTitulo = new JLabel("Gestor de Horarios - K-Coloración de Grafos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblTitulo, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 235, 242));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        lblEstado = new JLabel("Estado: Listo");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(new Color(60, 90, 154));

        panel.add(lblEstado, BorderLayout.WEST);

        return panel;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isArmed()) {
                    g2.setColor(new Color(
                            Math.max(0, color.getRed() - 30),
                            Math.max(0, color.getGreen() - 30),
                            Math.max(0, color.getBlue() - 30)));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                            Math.min(255, color.getRed() + 20),
                            Math.min(255, color.getGreen() + 20),
                            Math.min(255, color.getBlue() + 20)));
                } else {
                    g2.setColor(color);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };

        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setPreferredSize(new Dimension(150, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

  
}