package src;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.LinkedHashMap;

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
    private JComboBox<Integer> cmbGradoSelector;
    private JButton btnImportar;
    private JButton btnReiniciar;

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
        Integer gradoSeleccionadoAntes = (Integer) cmbGradoSelector.getSelectedItem();
        Integer gradoSeleccionado = (Integer) cmbGradoSelector.getSelectedItem();
        tabbedPanelHorarios.removeAll();

        if (gradoSeleccionado == null) {
            mostrarPlaceholderCrearHorario();
            return;
        }

        List<GrupoEstudiantes> gruposDelGrado = catalogo.getGruposPorGrado(gradoSeleccionado).stream()
                .sorted(Comparator.comparing(GrupoEstudiantes::getNombre))
                .collect(Collectors.toList());

        if (gruposDelGrado.isEmpty()) {
            // Podríamos mostrar un placeholder específico para el grado
            return;
        }

        // 1. Añadir la vista general del grado
        agregarPestanaGrado("Vista General " + gradoSeleccionado + "° Grado", gruposDelGrado);
        // 2. Añadir pestañas para cada grupo individual de ese grado
        gruposDelGrado.forEach(this::agregarPestanaHorario);
    }

    private void agregarPestanaHorario(GrupoEstudiantes grupo) {
        PanelHorario panel = new PanelHorario();
        List<BloqueHorario> bloquesDelGrupo = catalogo.getBloquesByGrupoId(grupo.getId());
        panel.cargarBloques(bloquesDelGrupo);
        tabbedPanelHorarios.addTab(grupo.getNombre(), panel);
    }

    private void agregarPestanaGrado(String titulo, List<GrupoEstudiantes> grupos) {
        List<String> idsGrupos = grupos.stream().map(GrupoEstudiantes::getId).collect(Collectors.toList());
        List<BloqueHorario> bloquesDelGrado = catalogo.getBloquesByGrupoIds(idsGrupos);
        PanelHorarioGrado panelGrado = new PanelHorarioGrado(grupos, bloquesDelGrado);
        tabbedPanelHorarios.addTab(titulo, new JScrollPane(panelGrado));
    }

    private void abrirCatalogoRecursos() {
        mostrarDialogoCatalogoRecursos();
    }

    private void onNuevoGrupo() {
        String nombreGrupo = JOptionPane.showInputDialog(this,
                "Ingrese el nombre del nuevo grupo (e.g., 1B, 3A):",
                "Nuevo Grupo", JOptionPane.PLAIN_MESSAGE);

        if (nombreGrupo != null && !nombreGrupo.trim().isEmpty()) {            
            if (catalogo.findGrupoByName(nombreGrupo.trim()).isPresent()) {
                JOptionPane.showMessageDialog(this, "El grupo '" + nombreGrupo.trim() + "' ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            GrupoEstudiantes nuevoGrupo = new GrupoEstudiantes(nombreGrupo.trim(), 1); // Grado por defecto, se edita en config
            catalogo.addGrupo(nuevoGrupo);

            // Si la pestaña de "Inicio" está presente, la eliminamos para dar paso a los horarios reales.
            if (tabbedPanelHorarios.getTabCount() == 1 && tabbedPanelHorarios.getTitleAt(0).equals("Inicio")) {
                tabbedPanelHorarios.removeAll();
            }

            // Recargamos todas las pestañas para que se organice por grado
            cargarPestanasDeGrupos();
            tabbedPanelHorarios.setSelectedIndex(tabbedPanelHorarios.getTabCount() - 1);
        }
    }

    private void onCrearHorario() {
        if (catalogo.getTodosLosBloques().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay asignaciones académicas creadas. Vaya a 'Catálogo de recursos' para crearlas.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tabSeleccionado = tabbedPanelHorarios.getSelectedIndex();
        if (tabSeleccionado == -1) {
             JOptionPane.showMessageDialog(this, "Seleccione una pestaña de grado para continuar.", "Acción no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tituloPestana = tabbedPanelHorarios.getTitleAt(tabSeleccionado);
        
        Integer grado = (Integer) cmbGradoSelector.getSelectedItem();
        if (grado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grado del menú superior.", "Acción no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Crear una pestaña temporal para la animación
        String tituloAnimacion = "Generando para " + grado + "° Grado...";
        PanelHorario panelAnimacion = new PanelHorario();
        // Deshabilitar el drop en el panel de animación para evitar interacciones
        panelAnimacion.setDropTarget(null);

        tabbedPanelHorarios.addTab(tituloAnimacion, panelAnimacion);
        tabbedPanelHorarios.setSelectedComponent(panelAnimacion);

        lblEstado.setText("Estado: Generando horario para " + grado + "° Grado...");

        // Usar SwingWorker para no congelar la UI
        SwingWorker<HorarioSemana, Void> worker = new SwingWorker<>() {
            @Override
            protected HorarioSemana doInBackground() throws Exception {
                // Obtener los bloques solo para el grado seleccionado
                List<GrupoEstudiantes> gruposDelGrado = catalogo.getGruposPorGrado(grado);
                List<String> idsGrupos = gruposDelGrado.stream().map(GrupoEstudiantes::getId).collect(Collectors.toList());
                List<BloqueHorario> bloquesDelGrado = catalogo.getBloquesByGrupoIds(idsGrupos);

                AdaptadorGraficaDeHorarios adaptador = new AdaptadorGraficaDeHorarios(bloquesDelGrado, catalogo);
                EstrategiaGeneracion estrategia = new EstrategiaColoracion();
                return estrategia.generarHorario(adaptador);
            }

            @Override
            protected void done() {
                HorarioSemana horarioGenerado = null;
                try {
                    horarioGenerado = get();
                    lblEstado.setText("Estado: Generación completada. Iniciando animación...");

                    panelAnimacion.cargarBloques(horarioGenerado.getBloques());

                    // Iniciar la animación con los resultados
                    AnimadorHorario animador = new AnimadorHorario(panelAnimacion, horarioGenerado.getBloques(), lblEstado, () -> {
                        // Al finalizar, eliminar la pestaña de animación y recargar las vistas de grado
                        SwingUtilities.invokeLater(() -> tabbedPanelHorarios.remove(panelAnimacion));
                        refrescarDatosYBloquesExistentes();
                        // Seleccionar la pestaña del grado que se acaba de generar
                        for (int i = 0; i < tabbedPanelHorarios.getTabCount(); i++) { //
                            if (tabbedPanelHorarios.getTitleAt(i).startsWith(String.valueOf(grado))) {
                                tabbedPanelHorarios.setSelectedIndex(i);
                                break;
                            }
                        }
                    });
                    animador.iniciar();

                } catch (Exception e) {
                    lblEstado.setText("Estado: Error en la generación del horario.");
                    if (panelAnimacion.getParent() == tabbedPanelHorarios) {
                        tabbedPanelHorarios.remove(panelAnimacion); // Limpiar en caso de error
                    }
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
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

    private void onReiniciarHorario() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas reiniciar todos los horarios?\n" +
                "Todos los bloques volverán a la sección 'sin asignar'.",
                "Confirmar Reinicio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Recarga todas las asignaciones desde el catálogo, lo que efectivamente reinicia los bloques.
            catalogo.getTodosLosGrupos().forEach(grupo -> {
                catalogo.getAsignacionesPorGrupo(grupo.getId()).forEach(catalogo::reconstruirBloquesDeAsignacion);
            });
            refrescarDatosYBloquesExistentes();
        }
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
        refrescarDatosYBloquesExistentes();
    }

    private void refrescarDatosYBloquesExistentes() {
        // Guardar la pestaña seleccionada para restaurarla después
        int tabSeleccionada = tabbedPanelHorarios.getSelectedIndex();

        actualizarSelectorDeGrado();
        cargarPestanasDeGrupos();
    }

    private void actualizarSelectorDeGrado() {
        Integer gradoSeleccionadoAntes = (Integer) cmbGradoSelector.getSelectedItem();
        
        List<Integer> grados = catalogo.getTodosLosGrupos().stream()
                .map(GrupoEstudiantes::getGrado)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        cmbGradoSelector.setModel(new DefaultComboBoxModel<>(grados.toArray(new Integer[0])));

        if (grados.contains(gradoSeleccionadoAntes)) {
            cmbGradoSelector.setSelectedItem(gradoSeleccionadoAntes);
        } else if (!grados.isEmpty()) {
            cmbGradoSelector.setSelectedIndex(0);
        } else {
            cmbGradoSelector.setSelectedItem(null);
        }
    }

    private Component crearComponentePestana(String titulo, Color color) {
        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTab.setOpaque(false);
        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        // Un pequeño círculo de color para identificar la pestaña
        JPanel colorIndicator = new JPanel();
        colorIndicator.setPreferredSize(new Dimension(10, 10));
        colorIndicator.setBackground(color);
        colorIndicator.setBorder(BorderFactory.createLineBorder(color.darker()));

        pnlTab.add(colorIndicator);
        pnlTab.add(Box.createHorizontalStrut(5));
        pnlTab.add(lblTitle);
        return pnlTab;
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

        // Selector de Grado
        cmbGradoSelector = new JComboBox<>();
        cmbGradoSelector.setPreferredSize(new Dimension(120, 30));
        cmbGradoSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbGradoSelector.addActionListener(e -> cargarPestanasDeGrupos());
        botonesPanel.add(new JLabel("Ver Grado:"));
        botonesPanel.add(cmbGradoSelector);

        btnConfiguracion = crearBoton("Catalogo de recursos", COLOR_ACCION_PRINCIPAL);
        btnNuevoGrupo = crearBoton("Agregar grupo", COLOR_ACCION_SECUNDARIA);
        btnCrearHorario = crearBoton("Planificar semana", COLOR_ACCION_ALERTA);
        btnExportar = crearBoton("Compartir horario", COLOR_ACCION_DORADO);
        btnImportar = crearBoton("Importar plantilla", COLOR_ACCION_VIOLETA);
        btnReiniciar = crearBoton("Reiniciar Horario", new Color(220, 53, 69));

        btnConfiguracion.addActionListener(e -> abrirCatalogoRecursos());
        btnNuevoGrupo.addActionListener(e -> onNuevoGrupo());
        btnCrearHorario.addActionListener(e -> onCrearHorario());
        btnExportar.addActionListener(e -> onExportar());
        btnImportar.addActionListener(e -> onImportar());
        btnReiniciar.addActionListener(e -> onReiniciarHorario());

        botonesPanel.add(btnConfiguracion);
        botonesPanel.add(btnNuevoGrupo);
        botonesPanel.add(btnCrearHorario);
        botonesPanel.add(btnExportar);
        botonesPanel.add(btnImportar);

        panel.add(botonesPanel, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        eastPanel.setBackground(COLOR_FONDO);

        JLabel lblTitulo = new JLabel("Planificador academico basado en k-coloracion");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setHorizontalAlignment(SwingConstants.RIGHT);

        eastPanel.add(lblTitulo);
        eastPanel.add(btnReiniciar);
        panel.add(eastPanel, BorderLayout.EAST);

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
