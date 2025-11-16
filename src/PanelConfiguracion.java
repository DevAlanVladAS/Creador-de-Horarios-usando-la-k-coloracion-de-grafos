package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Panel de configuración específico para la gestión de Profesores,
 * incluyendo Materia asignada y Horas de disponibilidad.
 */
public class PanelConfiguracion extends JPanel {

    // Horario del director: de 7 AM a 3 PM
    private final String[] HORAS_CLASE = {"7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00"};
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
    
    // Componentes del formulario
    private JTextField txtNombreProfesor;
    private JTextField txtMateriaAsignada;
    private JCheckBox[] checkDias;
    private JCheckBox[] checkHoras;
    private JButton btnGuardarProfesor;
    private JButton btnSinPreferencias;
    
    // Componentes de la tabla de gestión
    private JTable tablaProfesores;
    private DefaultTableModel modeloTabla;
    // Lista de grupos disponibles para asignar al guardar un profesor
    private JList<String> listaGrupos;
    private DefaultListModel<String> modeloListaGrupos;
    // Dialogo padre (si existe) para operaciones de Guardar y Salir
    private JDialog parentDialog;

    // Referencia al catálogo central
    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance(); 

    public PanelConfiguracion() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(850, 550));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Gestión de Profesores (Recursos Centrales)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6); 
        
        splitPane.setLeftComponent(crearPanelListadoProfesores());
    splitPane.setRightComponent(crearPanelFormulario());
        
        add(splitPane, BorderLayout.CENTER);
        
        // Cargar los datos existentes del catálogo al iniciar
        cargarDatosTabla();
        refreshListaGrupos();
    }
    
    // ==========================================================
    // CREACIÓN DE PANELES
    // ==========================================================

    private JPanel crearPanelListadoProfesores() {
        JPanel panelListado = new JPanel(new BorderLayout(5, 5));
        panelListado.setBorder(BorderFactory.createTitledBorder("Profesores Existentes en Catálogo"));
        
        String[] columnas = {"Nombre", "Materia", "Disponibilidad"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProfesores = new JTable(modeloTabla);
        tablaProfesores.getTableHeader().setReorderingAllowed(false);
        
        panelListado.add(new JScrollPane(tablaProfesores), BorderLayout.CENTER);
        
        // Botones de Acción (Editar/Eliminar)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        
        btnEliminar.addActionListener(e -> eliminarProfesorSeleccionado());
        
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnEliminar);
        panelListado.add(panelAcciones, BorderLayout.SOUTH);
        
        return panelListado;
    }

    private JPanel crearPanelFormulario() {
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Añadir Nuevo Profesor"));
        
        JPanel panelInputs = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // 1. Nombre del Profesor
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelInputs.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNombreProfesor = new JTextField(15);
        panelInputs.add(txtNombreProfesor, gbc);
        
        // 2. Materia Asignada
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelInputs.add(new JLabel("Materia:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtMateriaAsignada = new JTextField(15);
        panelInputs.add(txtMateriaAsignada, gbc);
        
        // 3. Disponibilidad por Días (Checkboxes)
        JPanel panelDias = crearPanelDisponibilidad(DIAS_SEMANA, "Días Disponibles", checkDias -> this.checkDias = checkDias);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 0;
        panelInputs.add(panelDias, gbc);
        
        // 4. Disponibilidad por Horas (Checkboxes)
        JPanel panelHoras = crearPanelDisponibilidad(HORAS_CLASE, "Horas Disponibles (7h-15h)", checkHoras -> this.checkHoras = checkHoras);
        gbc.gridy = 3; gbc.weighty = 1; // Permite que este panel ocupe el espacio restante
        panelInputs.add(panelHoras, gbc);

    // 5. Grupos existentes (selección múltiple)
    gbc.gridy = 4; gbc.gridwidth = 2; gbc.weighty = 0;
    JPanel panelGrupos = new JPanel(new BorderLayout());
    panelGrupos.setBorder(BorderFactory.createTitledBorder("Grupos existentes (seleccione para asignar)"));
    modeloListaGrupos = new DefaultListModel<>();
    listaGrupos = new JList<>(modeloListaGrupos);
    listaGrupos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    panelGrupos.add(new JScrollPane(listaGrupos), BorderLayout.CENTER);
    panelInputs.add(panelGrupos, gbc);
        
        panelFormulario.add(panelInputs, BorderLayout.NORTH);
        
        // 6. Botones de Acción (Guardar, Guardar y Salir, Salir, Sin Preferencias)
        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnGuardarProfesor = new JButton("Guardar Profesor");
        btnGuardarProfesor.addActionListener(e -> guardarProfesor(false));
        
        JButton btnGuardarYSalir = new JButton("Guardar y Salir");
        btnGuardarYSalir.addActionListener(e -> guardarProfesor(true));

        JButton btnSalir = new JButton("Salir");
        btnSalir.addActionListener(e -> {
            // Si hay datos en el formulario, confirmar pérdida de cambios
            boolean hayDatos = !txtNombreProfesor.getText().trim().isEmpty() || !txtMateriaAsignada.getText().trim().isEmpty();
            if (hayDatos) {
                int r = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres salir? Los cambios no guardados se borrarán.", "Confirmar salida", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION && parentDialog != null) {
                    parentDialog.dispose();
                }
            } else {
                if (parentDialog != null) parentDialog.dispose();
            }
        });
        
        btnSinPreferencias = new JButton("Sin Preferencias (Todo el Horario)");
        btnSinPreferencias.addActionListener(e -> setSinPreferencias());
        
        panelBotonesAccion.add(btnGuardarProfesor);
        panelBotonesAccion.add(btnGuardarYSalir);
        panelBotonesAccion.add(btnSalir);
        panelBotonesAccion.add(btnSinPreferencias);
        panelFormulario.add(panelBotonesAccion, BorderLayout.SOUTH);

        return panelFormulario;
    }
    
    // Función auxiliar para crear paneles de checkboxes (Días u Horas)
    private JPanel crearPanelDisponibilidad(String[] items, String titulo, java.util.function.Consumer<JCheckBox[]> setter) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        
        JCheckBox[] checks = new JCheckBox[items.length];
        for (int i = 0; i < items.length; i++) {
            checks[i] = new JCheckBox(items[i], true);
            panel.add(checks[i]);
        }
        setter.accept(checks); // Asigna los checks al campo de la clase (checkDias o checkHoras)
        return panel;
    }

    // ==========================================================
    // LÓGICA DE DATOS Y ACCIÓN
    // ==========================================================
    
    private void setSinPreferencias() {
        // Deseleccionar todas las opciones (al guardarlo, se interpreta como disponibilidad total)
        for (JCheckBox check : checkDias) {
            check.setSelected(false);
        }
        for (JCheckBox check : checkHoras) {
            check.setSelected(false);
        }
        JOptionPane.showMessageDialog(this, "Las preferencias han sido deseleccionadas.\nAl guardar, el profesor se considerará disponible todos los días y horas.", "Sin Preferencias", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        
        // Cargar desde el catálogo central
        for (Profesor p : catalogo.getTodosLosProfesores()) {
            Vector<String> row = new Vector<>();
            row.add(p.getNombre());
            row.add(p.getMateriaAsignada());
            
            // Unir días y horas en una sola cadena de Disponibilidad
            String diasStr = p.getDiasDisponibles().isEmpty() ? "Todos los días" : String.join(", ", p.getDiasDisponibles());
            String horasStr = p.getHorasDisponibles().isEmpty() ? "Todas las horas" : String.join(", ", p.getHorasDisponibles());
            
            row.add(diasStr + " / " + horasStr);
            
            modeloTabla.addRow(row);
        }
        // Actualizar lista de grupos en el formulario
        refreshListaGrupos();
    }

    private void refreshListaGrupos() {
        if (modeloListaGrupos == null) return;
        modeloListaGrupos.clear();
        for (GrupoEstudiantes g : catalogo.getTodosLosGrupos()) {
            modeloListaGrupos.addElement(g.getNombre());
        }
    }

    private void guardarProfesor() {
        guardarProfesor(false);
    }

    private void guardarProfesor(boolean closeAfter) {
        String nombre = txtNombreProfesor.getText().trim();
        String materia = txtMateriaAsignada.getText().trim();
        
        if (nombre.isEmpty() || materia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y la materia del profesor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<String> diasSeleccionados = new ArrayList<>();
        for (JCheckBox check : checkDias) {
            if (check.isSelected()) {
                diasSeleccionados.add(check.getText());
            }
        }
        
        List<String> horasSeleccionadas = new ArrayList<>();
        for (JCheckBox check : checkHoras) {
            if (check.isSelected()) {
                horasSeleccionadas.add(check.getText());
            }
        }
        
        // Crear la instancia de Profesor con los nuevos campos
        Profesor nuevoProfesor = new Profesor(nombre, materia, diasSeleccionados, horasSeleccionadas);

        // Añadir al catálogo central
        catalogo.addProfesor(nuevoProfesor);

        // Si se seleccionaron grupos, asignar el profesor a esos grupos
        List<String> gruposSeleccionados = listaGrupos.getSelectedValuesList();
        if (gruposSeleccionados != null && !gruposSeleccionados.isEmpty()) {
            for (String nombreGrupo : gruposSeleccionados) {
                GrupoEstudiantes grupo = catalogo.getTodosLosGrupos().stream()
                    .filter(g -> g.getNombre().equals(nombreGrupo))
                    .findFirst().orElse(null);
                if (grupo != null) {
                    grupo.addProfesor(nuevoProfesor.getId());
                }
            }
        }

        // Refrescar la tabla
        cargarDatosTabla();

        // Limpiar formulario
        txtNombreProfesor.setText("");
        txtMateriaAsignada.setText("");
        for (JCheckBox check : checkDias) { check.setSelected(true); }
        for (JCheckBox check : checkHoras) { check.setSelected(true); }
        listaGrupos.clearSelection();

        JOptionPane.showMessageDialog(this,
            "Profesor '" + nombre + "' guardado y añadido al catálogo central.",
            "Guardado Exitoso",
            JOptionPane.INFORMATION_MESSAGE);

        if (closeAfter && parentDialog != null) {
            parentDialog.dispose();
        }
    }
    
    private void eliminarProfesorSeleccionado() {
        int filaSeleccionada = tablaProfesores.getSelectedRow();
        
        if (filaSeleccionada >= 0) {
            String nombreProf = (String) modeloTabla.getValueAt(filaSeleccionada, 0); // Obtener el nombre
            
            int respuesta = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar a '" + nombreProf + "' del catálogo?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                // Como no usamos ID en la tabla, necesitamos encontrar el profesor en el catálogo
                Profesor profesorAEliminar = catalogo.getTodosLosProfesores().stream()
                    .filter(p -> p.getNombre().equals(nombreProf))
                    .findFirst().orElse(null);
                
                if (profesorAEliminar != null) {
                    catalogo.removeProfesor(profesorAEliminar.getId());
                    cargarDatosTabla(); // Refrescar la tabla
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un profesor de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Este método ya no es necesario ya que los datos se almacenan en el Singleton,
    // pero lo dejamos para compatibilidad si la InterfazGrafica lo requiere.
    public List<Profesor> getProfesoresCreados() {
        return catalogo.getTodosLosProfesores();
    }

    /** Si este panel se muestra en un JDialog, asignarlo para poder cerrarlo desde los botones. */
    public void setParentDialog(JDialog dialog) {
        this.parentDialog = dialog;
    }
}