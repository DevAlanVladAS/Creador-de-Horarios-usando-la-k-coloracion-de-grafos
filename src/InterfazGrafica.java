package src;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import javax.imageio.ImageIO;

public class InterfazGrafica extends JFrame implements GestorHorarios.ValidationListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGrafica frame = new InterfazGrafica();
            frame.setVisible(true);
        });
    }

    private JTabbedPane tabbedPanelHorarios;
    private JButton btnCrearHorario;
    private JButton btnConfiguracion;
    private JButton btnExportar;
    private JComboBox<Integer> cmbGradoSelector;
    private JButton btnReiniciar;
    private JLabel lblTituloProyecto;

    private static final Color COLOR_FONDO = new Color(245, 248, 255);
    private static final Color COLOR_ACCION_PRINCIPAL = new Color(76, 110, 245);
    private static final Color COLOR_ACCION_ALERTA = new Color(255, 140, 66);
    private static final Color COLOR_ACCION_DORADO = new Color(255, 196, 0);

    private JLabel lblEstado;
    private PanelNotificaciones panelNotificaciones;

    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
    private final GestorHorarios gestor = GestorHorarios.getInstance();
    private ConfiguracionProyecto configuracionProyecto = new ConfiguracionProyecto();
    private final ControladorPersistencia persistenciaController = new ControladorPersistencia();
    

    public InterfazGrafica() {
        setTitle("Generador de Horarios");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initComponents();
        actualizarTituloProyecto();

        // Registrarse para escuchar resultados de validación
        gestor.addValidationListener(this);

        if (catalogo.getTodosLosGrupos().isEmpty()) {
            mostrarPlaceholderCrearHorario();
        } else {
            recargarDesdeCatalogo();
        }
    }

    @Override
    public void onValidationFinished(List<ResultadoValidacion> resultados) {
        // Cuando el gestor termina de validar, actualiza el panel de notificaciones.
        SwingUtilities.invokeLater(() -> panelNotificaciones.mostrarResultados(resultados));
    }

    private void sincronizarCatalogoConGestor() {
        List<GrupoEstudiantes> grupos = catalogo.getTodosLosGrupos();
        
        for (GrupoEstudiantes grupo : grupos) {
            String grupoId = grupo.getId();
            
            List<BloqueHorario> bloquesDelGrupo = catalogo.getBloquesByGrupoId(grupoId);
            
            
            HorarioSemana semana = gestor.getHorarioSemana(grupoId);
            // --- Sincronización no destructiva para preservar posiciones manuales ---
            // 1) Agregar los bloques nuevos que estén en catálogo y no en el gestor.
            var existentes = new ArrayList<>(semana.getBloques());
            var idsExistentes = existentes.stream().map(BloqueHorario::getId).collect(Collectors.toSet());
            for (BloqueHorario bloque : bloquesDelGrupo) {
                if (!idsExistentes.contains(bloque.getId())) {
                    gestor.agregarBloque(bloque, grupoId);
                }
                // Reaplicar la posición almacenada (si la hay) sin borrar otras posiciones.
                if (bloque.getDia() != null && bloque.getHoraInicio() != null) {
                    gestor.actualizarPosicionBloque(bloque, bloque.getDia(), bloque.getHoraInicio());
                } else if (bloque.getDia() == null) {
                    semana.agregarBloqueSinAsignar(bloque);
                }
            }

            // 2) Remover bloques que ya no existan en catálogo.
            var idsCatalogo = bloquesDelGrupo.stream().map(BloqueHorario::getId).collect(Collectors.toSet());
            for (BloqueHorario bloqueExistente : existentes) {
                if (!idsCatalogo.contains(bloqueExistente.getId())) {
                    semana.eliminarBloque(bloqueExistente.getId());
                }
            }
        }
    }

    private void cargarPestanasDeGrupos() {
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
            return;
        }


        agregarPestanaGrado("Vista General " + gradoSeleccionado + " Grado", gruposDelGrado);
        
  
        gruposDelGrado.forEach(this::agregarPestanaHorario);
    }

 
    private void agregarPestanaHorario(GrupoEstudiantes grupo) {
        PanelHorario panel = new PanelHorario(grupo.getId());
        tabbedPanelHorarios.addTab(grupo.getNombre(), panel);
    }

    private void agregarPestanaGrado(String titulo, List<GrupoEstudiantes> grupos) {
        PanelHorarioGrado panelGrado = new PanelHorarioGrado(grupos);
        tabbedPanelHorarios.addTab(titulo, panelGrado);
    }

    private void abrirCatalogoRecursos() {
        mostrarDialogoCatalogoRecursos();
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
            JOptionPane.showMessageDialog(this, "Seleccione una pestaña de grado para continuar.", 
                "Acción no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Integer grado = (Integer) cmbGradoSelector.getSelectedItem();
        if (grado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grado del menú superior.", 
                "Acción no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        lblEstado.setText("Estado: Generando horario para " + grado + "Grado...");

        // Mostrar un diálogo de "cargando" para bloquear la UI
        JDialog dlgCargando = crearDialogoCargando("Generando para " + grado + "Grado...");
        
        SwingWorker<HorarioSemana, Void> worker = new SwingWorker<>() {
            @Override
            protected HorarioSemana doInBackground() throws Exception {
                List<GrupoEstudiantes> gruposDelGrado = catalogo.getGruposPorGrado(grado);
                List<String> idsGrupos = gruposDelGrado.stream()
                    .map(GrupoEstudiantes::getId)
                    .collect(Collectors.toList());
                List<BloqueHorario> bloquesDelGrado = catalogo.getBloquesByGrupoIds(idsGrupos);

                AdaptadorGraficaDeHorarios adaptador = new AdaptadorGraficaDeHorarios(bloquesDelGrado, catalogo);
                EstrategiaGeneracion estrategia = new EstrategiaColoracion();
                return estrategia.generarHorario(adaptador);
            }

            @Override
            protected void done() {
                try {
                    dlgCargando.setVisible(false); // Ocultar diálgoo al iniciar procesamiento
                    HorarioSemana horarioGenerado = get();
                    lblEstado.setText("Estado: Generación completada. Iniciando animación...");

                    List<BloqueHorario> bloques = horarioGenerado.getBloques();
                    
                    for (BloqueHorario bloque : bloques) {
                        String grupoId = bloque.getGrupoId();
                        
                        
                        if (!gestor.buscarBloquePorId(bloque.getId()).isPresent()) {
                            gestor.agregarBloque(bloque, grupoId);
                        }
                    }

                    AnimadorHorario animador = new AnimadorHorario(
                        bloques, 
                        lblEstado, 
                        () -> {
                            SwingUtilities.invokeLater(() -> {
                                sincronizarBloquesGenerados(bloques);
                                refrescarDatosYBloquesExistentes();
                                
                                for (int i = 0; i < tabbedPanelHorarios.getTabCount(); i++) {
                                    if (tabbedPanelHorarios.getTitleAt(i).startsWith(String.valueOf(grado))) {
                                        tabbedPanelHorarios.setSelectedIndex(i);
                                        break;
                                    }
                                }
                                dlgCargando.dispose();
                            });
                        }
                    );
                    animador.iniciar();

                } catch (Exception e) {
                    dlgCargando.dispose();
                    lblEstado.setText("Estado: Error en la generación del horario.");                    
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(InterfazGrafica.this,
                        "Error al generar horario: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Ejecutar worker y mostrar diálogo de carga
        worker.execute();
        dlgCargando.setVisible(true);
    }

    private JDialog crearDialogoCargando(String titulo) {
        JDialog dialog = new JDialog(this, "Procesando...", true);
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(new JProgressBar(0, 0) {{ setIndeterminate(true); }}, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        return dialog;
    }

    private void sincronizarBloquesGenerados(List<BloqueHorario> bloquesGenerados) {
        for (BloqueHorario bloque : bloquesGenerados) {
            BloqueHorario bloqueEnGestor = gestor.buscarBloquePorId(bloque.getId()).orElse(null);
            if (bloqueEnGestor != null) {
                gestor.actualizarPosicionBloque(bloqueEnGestor, bloque.getDia(), bloque.getHoraInicio());
            }
        }
    }

    private void onExportar() {
        Component vistaActual = tabbedPanelHorarios.getSelectedComponent();
        if (vistaActual == null) {
            JOptionPane.showMessageDialog(this, "No hay una vista de horario seleccionada para exportar.", "Exportar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Exportar una vista consolidada de grado en archivos separados por grupo
        if (vistaActual instanceof PanelHorarioGrado panelGrado) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecciona carpeta destino para los PNG de cada grupo");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File directorio = chooser.getSelectedFile();
            int exportados = 0;
            List<GrupoEstudiantes> grupos = panelGrado.getGrupos();
            for (GrupoEstudiantes grupo : grupos) {
                PanelHorario panelTmp = new PanelHorario(grupo.getId());
                String nombre = (grupo.getNombre() != null ? grupo.getNombre() : "grupo") + ".png";
                File archivoGrupo = new File(directorio, nombre);
                try {
                    exportarComponenteComoPNG(panelTmp, archivoGrupo);
                    exportados++;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Error al exportar el grupo " + grupo.getNombre() + ": " + e.getMessage(),
                            "Error de Exportación", JOptionPane.ERROR_MESSAGE);
                } finally {
                    panelTmp.dispose();
                }
            }

            if (exportados > 0) {
                JOptionPane.showMessageDialog(this,
                        "Exportados " + exportados + " grupos en:\n" + directorio.getAbsolutePath(),
                        "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        // Exportar la pestaña actual (grupo individual)
        Component panelAExportar = vistaActual;
        if (panelAExportar instanceof JScrollPane) {
            JViewport viewport = ((JScrollPane) panelAExportar).getViewport();
            if (viewport != null && viewport.getView() != null) {
                panelAExportar = viewport.getView();
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar horario como PNG");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagen PNG", "png"));

        int resultado = chooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".png")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
            }

            try {
                exportarComponenteComoPNG(panelAExportar, archivo);
                JOptionPane.showMessageDialog(this, "Horario exportado exitosamente a:\n" + archivo.getAbsolutePath(), "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ocurrió un error al guardar la imagen:\n" + e.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
        private void exportarComponenteComoPNG(Component component, File archivo) throws IOException {
        Dimension size = component.getSize();
        if (size.width <= 0 || size.height <= 0) {
            size = component.getPreferredSize();
            component.setSize(size);
            component.doLayout();
        }
        BufferedImage imagen = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagen.createGraphics();
        component.printAll(g2d);
        g2d.dispose();
        ImageIO.write(imagen, "png", archivo);
    }

    private void onReiniciarHorario() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "\u00bfEst\u00e1s seguro de que deseas reiniciar todos los horarios?\n" +
                "Todos los bloques volver\u00e1n a la secci\u00f3n 'sin asignar'.",
                "Confirmar Reinicio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            gestor.limpiarTodo();
            catalogo.getTodosLosGrupos().forEach(grupo -> {
                catalogo.getAsignacionesPorGrupo(grupo.getId())
                    .forEach(catalogo::reconstruirBloquesDeAsignacion);
            });
            recargarDesdeCatalogo();
            lblEstado.setText("Estado: Horarios reiniciados. Todos los bloques sin asignar.");
        }
    }
private void mostrarPlaceholderCrearHorario() {
        tabbedPanelHorarios.removeAll();
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Configura tu nuevo proyecto escolar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

       

        JButton btnConfigurarProyecto = new JButton("Configurar nuevo proyecto");
        btnConfigurarProyecto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfigurarProyecto.addActionListener(e -> mostrarDialogoNuevoProyecto());

        JButton btnCatalogo = new JButton("Gestionar catalogo de recursos");
        btnCatalogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCatalogo.addActionListener(e -> mostrarDialogoCatalogoRecursos());

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
      
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(btnConfigurarProyecto);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(btnCatalogo);
        panel.add(Box.createVerticalGlue());

        tabbedPanelHorarios.addTab("Inicio", panel);
    }

    private void mostrarDialogoNuevoProyecto() {
        PanelConfiguracionProyecto panelProyecto = new PanelConfiguracionProyecto();
        panelProyecto.setConfiguracion(configuracionProyecto);

        JDialog dialog = new JDialog(this, "Configurar nuevo proyecto", true);
        panelProyecto.addGestionarRecursosListener(e -> mostrarDialogoCatalogoRecursos(dialog));
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panelProyecto, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Crear proyecto");
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);
        dialog.getContentPane().add(acciones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            ConfiguracionProyecto nuevaConfig = panelProyecto.construirConfiguracion();
            if (!nuevaConfig.estaCompleta()) {
                JOptionPane.showMessageDialog(dialog,
                        "Indica el nombre de la escuela para continuar.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int totalGrupos = 0;
            for (int grado = 1; grado <= 3; grado++) {
                totalGrupos += nuevaConfig.getCantidadGrupos(grado);
            }
            if (totalGrupos == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Configura al menos un grupo para crear el proyecto.",
                        "Sin grupos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (aplicarConfiguracionProyecto(nuevaConfig, true)) {
                dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public boolean aplicarConfiguracionProyecto(ConfiguracionProyecto nuevaConfig, boolean limpiarExistente) {
        establecerConfiguracionProyecto(nuevaConfig);

        if (limpiarExistente) {
            gestor.limpiarTodo();
            catalogo.getAsignaciones().forEach(asignacion -> catalogo.removeAsignacion(asignacion.getId()));
            catalogo.getTodosLosGrupos().forEach(grupo -> catalogo.removeGrupo(grupo.getId()));
            catalogo.getTodosLosBloques().forEach(bloque -> catalogo.removeBloqueHorario(bloque.getId()));
        }
        
        crearGruposIniciales(configuracionProyecto);
        recargarDesdeCatalogo();
        return true;
    }

    public void establecerConfiguracionProyecto(ConfiguracionProyecto nuevaConfig) {
        if (nuevaConfig == null) {
            configuracionProyecto = new ConfiguracionProyecto();
        } else {
            configuracionProyecto = nuevaConfig;
        }
        actualizarTituloProyecto();
    }

    public void recargarDesdeCatalogo() {
        sincronizarCatalogoConGestor();
        refrescarDatosYBloquesExistentes();
    }

    private void crearGruposIniciales(ConfiguracionProyecto config) {
        for (int grado = 1; grado <= 3; grado++) {
            int cantidad = config.getCantidadGrupos(grado);
            for (int indice = 0; indice < cantidad; indice++) {
                GrupoEstudiantes grupo = new GrupoEstudiantes(generarNombreGrupo(indice), grado);
                catalogo.addGrupo(grupo);
            }
        }
    }

    private String generarNombreGrupo(int indice) {
        StringBuilder etiqueta = new StringBuilder();
        int numero = indice;
        do {
            int residuo = numero % 26;
            etiqueta.insert(0, (char) ('A' + residuo));
            numero = (numero / 26) - 1;
        } while (numero >= 0);
        return etiqueta.toString();
    }

    private void actualizarTituloProyecto() {
        if (lblTituloProyecto == null) return;
        
        if (configuracionProyecto != null && configuracionProyecto.estaCompleta()) {
            lblTituloProyecto.setText(configuracionProyecto.getNombreEscuela() + " - Planificador academico");
        } else {
            lblTituloProyecto.setText("Generador de Horarios");
        }
    }

    private void mostrarDialogoCatalogoRecursos() {
        mostrarDialogoCatalogoRecursos(this);
    }

    private void mostrarDialogoCatalogoRecursos(Window parentWindow) {
        Window owner = parentWindow != null ? parentWindow : this;
        JDialog dialog = new JDialog(owner, "Catalogo de recursos", Dialog.ModalityType.DOCUMENT_MODAL);
        PanelConfiguracion panelConfig = new PanelConfiguracion(true);
        
        dialog.getContentPane().add(panelConfig);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        recargarDesdeCatalogo();
    }

    private void refrescarDatosYBloquesExistentes() {
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
        JMenuItem itemGuardar = new JMenuItem("Guardar como...");
        JMenuItem itemExportarPNG = new JMenuItem("Exportar a PNG");
        JMenuItem itemSalir = new JMenuItem("Salir");

        itemNuevo.addActionListener(e -> mostrarDialogoNuevoProyecto());
        itemAbrir.addActionListener(e -> onAbrirProyecto());
        itemGuardar.addActionListener(e -> onGuardarProyecto());
        itemExportarPNG.addActionListener(e -> onExportar());
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemExportarPNG);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        


        menuBar.add(menuArchivo);
        menuBar.add(Box.createHorizontalGlue());

        return menuBar;
    }

    private void onGuardarProyecto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar proyecto");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo de proyecto JSON", "json"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".json")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".json");
            }

            ProyectoDatos datos = new ProyectoDatos(configuracionProyecto, catalogo);
            try {
                persistenciaController.guardarProyecto(datos, archivo.getAbsolutePath());
                lblEstado.setText("Estado: Proyecto guardado correctamente.");
                JOptionPane.showMessageDialog(this, "Proyecto guardado exitosamente.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar el proyecto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAbrirProyecto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir proyecto");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo de proyecto JSON", "json"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                ProyectoDatos datosCargados = persistenciaController.cargarProyecto(archivo.getAbsolutePath());
                
                // Limpiar estado actual
                gestor.limpiarTodo();
                catalogo.reset();

                // Restaurar estado desde los datos cargados
                datosCargados.restaurarEn(this, catalogo);
                lblEstado.setText("Estado: Proyecto cargado correctamente.");

                JOptionPane.showMessageDialog(this, "Proyecto cargado exitosamente.", "Cargado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al abrir el proyecto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        botonesPanel.setBackground(COLOR_FONDO);

        cmbGradoSelector = new JComboBox<>();
        cmbGradoSelector.setPreferredSize(new Dimension(120, 30));
        cmbGradoSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbGradoSelector.addActionListener(e -> cargarPestanasDeGrupos());
        botonesPanel.add(new JLabel("Ver Grado:"));
        botonesPanel.add(cmbGradoSelector);

        btnConfiguracion = crearBoton("Catalogo de recursos", COLOR_ACCION_PRINCIPAL);
        btnCrearHorario = crearBoton("Generar Horario", COLOR_ACCION_ALERTA);
        btnExportar = crearBoton("Exportar Horario", COLOR_ACCION_DORADO);
        btnReiniciar = crearBoton("Reiniciar Horario", new Color(220, 53, 69));

        btnConfiguracion.addActionListener(e -> abrirCatalogoRecursos());
        btnCrearHorario.addActionListener(e -> onCrearHorario());
        btnExportar.addActionListener(e -> onExportar());
        btnReiniciar.addActionListener(e -> onReiniciarHorario());

        botonesPanel.add(btnConfiguracion);
        botonesPanel.add(btnCrearHorario);
        botonesPanel.add(btnExportar);

        panel.add(botonesPanel, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        eastPanel.setBackground(COLOR_FONDO);

        lblTituloProyecto = new JLabel("Generador de Horarios");
        lblTituloProyecto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloProyecto.setHorizontalAlignment(SwingConstants.RIGHT);

        eastPanel.add(lblTituloProyecto);
        eastPanel.add(btnReiniciar);
        panel.add(eastPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(230, 235, 242));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        lblEstado = new JLabel("Estado: listo");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(new Color(60, 90, 154));
        panel.add(lblEstado, BorderLayout.WEST);

        panelNotificaciones = new PanelNotificaciones();
        panel.add(panelNotificaciones, BorderLayout.CENTER);
        
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
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
