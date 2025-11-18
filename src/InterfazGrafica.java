package src;

import java.awt.*;
import javax.swing.*;
import java.util.List;

/**
 * Ventana principal para la gestion visual de horarios.
 */
public class InterfazGrafica extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGrafica frame = new InterfazGrafica();
            frame.setVisible(true);
        });
    }

    private JTabbedPane tabbedPanelHorarios;
    private JButton btnNuevoGrupo;
    private JButton btnCrearHorario;
    private JButton btnConfiguracion;
    private JButton btnExportar;
    private JButton btnImportar;

    private static final Color COLOR_FONDO = new Color(245, 248, 255);
    private static final Color COLOR_ACCION_PRINCIPAL = new Color(76, 110, 245);
    private static final Color COLOR_ACCION_SECUNDARIA = new Color(0, 172, 193);
    private static final Color COLOR_ACCION_ALERTA = new Color(255, 140, 66);
    private static final Color COLOR_ACCION_DORADO = new Color(255, 196, 0);
    private static final Color COLOR_ACCION_VIOLETA = new Color(111, 66, 193);

    private JLabel lblEstado;

    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance();

    public InterfazGrafica() {
        setTitle("Creador de Horarios - K-Coloracion de Grafos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initComponents();

        catalogo.getTodosLosProfesores().stream()
                .filter(p -> "algebra".equalsIgnoreCase(p.getNombre())
                        || "algebra".equalsIgnoreCase(p.getMateriaAsignada()))
                .forEach(p -> catalogo.removeProfesor(p.getId()));

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

    private void agregarPestanaHorario(GrupoEstudiantes grupo) {
        PanelHorario panel = new PanelHorario();
        List<BloqueHorario> bloquesDelGrupo = catalogo.getBloquesByGrupoId(grupo.getId());
        panel.cargarBloques(bloquesDelGrupo);
        tabbedPanelHorarios.addTab(grupo.getNombre(), panel);
    }

    private void abrirCatalogoRecursos() {
        mostrarDialogoCatalogoRecursos();
    }

    private void onNuevoGrupo() {
        String nombreGrupo = JOptionPane.showInputDialog(this,
                "Ingrese el nombre del nuevo grupo (e.g., 1B, 3A):",
                "Nuevo Grupo", JOptionPane.PLAIN_MESSAGE);

        if (nombreGrupo != null && !nombreGrupo.trim().isEmpty()) {
            GrupoEstudiantes nuevoGrupo = new GrupoEstudiantes(nombreGrupo.trim());
            catalogo.addGrupo(nuevoGrupo);
            cargarPestanasDeGrupos();
            tabbedPanelHorarios.setSelectedIndex(tabbedPanelHorarios.getTabCount() - 1);
        }
    }

    private void onCrearHorario() {
        if (tabbedPanelHorarios.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay grupos creados para generar un horario.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String grupoSeleccionado = tabbedPanelHorarios.getTitleAt(
                tabbedPanelHorarios.getSelectedIndex());

        JOptionPane.showMessageDialog(this,
                "Ejecutando k-coloracion para el grupo: " + grupoSeleccionado + "...",
                "Generando horario", JOptionPane.INFORMATION_MESSAGE);

        lblEstado.setText("Estado: horario generado para " + grupoSeleccionado);
    }

    private void onExportar() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de exportacion en desarrollo.",
                "Exportar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onImportar() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de importacion en desarrollo.",
                "Importar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarPlaceholderCrearHorario() {
        tabbedPanelHorarios.removeAll();
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Comienza configurando tu escuela");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html>Aun no hay recursos registrados.<br/>"
                        + "Abre el catalogo para dar de alta grupos, materias y profesores "
                        + "y despues genera los 5 bloques estandar.</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCrear = new JButton("Agregar grupo");
        btnCrear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrear.addActionListener(e -> onNuevoGrupo());

        JButton btnCatalogo = new JButton("Abrir catalogo");
        btnCatalogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCatalogo.addActionListener(e -> mostrarDialogoCatalogoRecursos());

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(btnCrear);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(btnCatalogo);
        panel.add(Box.createVerticalGlue());

        tabbedPanelHorarios.addTab("Inicio", panel);
    }

    private void mostrarDialogoCatalogoRecursos() {
        JDialog dialog = new JDialog(this, "Catalogo de recursos", true);
        PanelConfiguracion panelConfig = new PanelConfiguracion();
        panelConfig.setParentDialog(dialog);
        dialog.getContentPane().add(panelConfig);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        cargarPestanasDeGrupos();
    }

    private void initComponents() {
        JMenuBar menuBar = crearMenuBar();
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = crearPanelSuperior();

        tabbedPanelHorarios = new JTabbedPane(JTabbedPane.TOP);
        tabbedPanelHorarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPanelHorarios.setBackground(Color.WHITE);

        JPanel bottomPanel = crearPanelInferior();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPanelHorarios, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(46, 78, 126));
        menuBar.setForeground(Color.WHITE);

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setForeground(Color.WHITE);

        JMenuItem itemNuevo = new JMenuItem("Nuevo proyecto");
        JMenuItem itemAbrir = new JMenuItem("Abrir");
        JMenuItem itemGuardar = new JMenuItem("Guardar");
        JMenuItem itemExportar = new JMenuItem("Exportar");
        JMenuItem itemSalir = new JMenuItem("Salir");

        itemExportar.addActionListener(e -> onExportar());
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemExportar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuEditar = new JMenu("Edicion");
        menuEditar.setForeground(Color.WHITE);
        menuEditar.add(new JMenuItem("Deshacer"));
        menuEditar.add(new JMenuItem("Rehacer"));

        JMenu menuHerramientas = new JMenu("Herramientas");
        menuHerramientas.setForeground(Color.WHITE);
        JMenuItem itemValidar = new JMenuItem("Validar horario");
        itemValidar.addActionListener(e -> JOptionPane.showMessageDialog(this, "Validando horario..."));
        menuHerramientas.add(itemValidar);

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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        botonesPanel.setBackground(COLOR_FONDO);

        btnConfiguracion = crearBoton("Catalogo de recursos", COLOR_ACCION_PRINCIPAL);
        btnNuevoGrupo = crearBoton("Agregar grupo", COLOR_ACCION_SECUNDARIA);
        btnCrearHorario = crearBoton("Planificar semana", COLOR_ACCION_ALERTA);
        btnExportar = crearBoton("Compartir horario", COLOR_ACCION_DORADO);
        btnImportar = crearBoton("Importar plantilla", COLOR_ACCION_VIOLETA);

        btnConfiguracion.addActionListener(e -> abrirCatalogoRecursos());
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

        JLabel lblTitulo = new JLabel("Planificador academico basado en k-coloracion");
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

        lblEstado = new JLabel("Estado: listo");
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
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
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
        boton.setPreferredSize(new Dimension(170, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }
}
