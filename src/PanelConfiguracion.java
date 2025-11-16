package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
    private JTextField txtHorasSemanales;
    private JCheckBox[] checkDias;
    private JCheckBox[] checkHoras;
    private List<JCheckBox> checkGrupos;
    private JButton btnGuardarProfesor;
    private JButton btnSinPreferencias;
    
    // Componentes de la tabla de gestión
    private JTable tablaProfesores;
    private DefaultTableModel modeloTabla;
    private JDialog parentDialog; // Para cerrar el diálogo al guardar

    // Estado de edición
    private Profesor profesorEnEdicion = null;

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
        btnEditar.addActionListener(e -> editarProfesorSeleccionado());
        
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
        
        // 3. Horas semanales
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelInputs.add(new JLabel("Horas por semana:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtHorasSemanales = new JTextField(5);
        panelInputs.add(txtHorasSemanales, gbc);

        // 4. Disponibilidad por Días (Checkboxes)
        JPanel panelDias = crearPanelDisponibilidad(DIAS_SEMANA, "Días Disponibles", checkDias -> this.checkDias = checkDias);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weighty = 0;
        panelInputs.add(panelDias, gbc);
        
        // 5. Asignación de Grupos
        JPanel panelGrupos = crearPanelGrupos();
        gbc.gridy = 4;
        panelInputs.add(panelGrupos, gbc);

        // 6. Disponibilidad por Horas (Checkboxes)
        JPanel panelHoras = crearPanelDisponibilidad(HORAS_CLASE, "Horas Disponibles (7h-15h)", checkHoras -> this.checkHoras = checkHoras);
        gbc.gridy = 5; gbc.weighty = 1; // Permite que este panel ocupe el espacio restante
        panelInputs.add(panelHoras, gbc);

        panelFormulario.add(panelInputs, BorderLayout.NORTH);
        
        // 5. Botones de Acción (Guardar y Sin Preferencias)
        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnGuardarProfesor = new JButton("Guardar Profesor");
        btnGuardarProfesor.addActionListener(e -> guardarProfesor());
        
        btnSinPreferencias = new JButton("Sin Preferencias (Todo el Horario)");
        btnSinPreferencias.addActionListener(e -> setSinPreferencias());
        
        panelBotonesAccion.add(btnGuardarProfesor);
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

    private JPanel crearPanelGrupos() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Asignar a Grupos"));
        checkGrupos = new ArrayList<>();

        List<GrupoEstudiantes> grupos = catalogo.getTodosLosGrupos();
        if (grupos.isEmpty()) {
            panel.add(new JLabel("No hay grupos creados."));
        } else {
            for (GrupoEstudiantes grupo : grupos) {
                JCheckBox check = new JCheckBox(grupo.getNombre());
                check.putClientProperty("grupoId", grupo.getId()); // Guardar ID para referencia
                checkGrupos.add(check);
                panel.add(check);
            }
        }
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
    }

    private void guardarProfesor() {
        String nombre = txtNombreProfesor.getText().trim();
        String materia = txtMateriaAsignada.getText().trim();
        
        if (nombre.isEmpty() || materia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre y la materia del profesor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int horasSemanales;
        try {
            horasSemanales = Integer.parseInt(txtHorasSemanales.getText().trim());
            if (horasSemanales <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Las horas por semana deben ser un número entero positivo.", "Error", JOptionPane.ERROR_MESSAGE);
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
        
        List<String> gruposSeleccionadosIds = checkGrupos.stream()
            .filter(JCheckBox::isSelected)
            .map(check -> (String) check.getClientProperty("grupoId"))
            .collect(Collectors.toList());

        // --- VALIDACIÓN DE HORAS ---
        // Cada bloque creado para un grupo cuenta como 1 hora.
        int horasAsignadasPreviamente = 0;
        if (profesorEnEdicion != null) {
            // Al editar, no contamos los bloques que vamos a reemplazar.
            // La validación se hace sobre el total de grupos seleccionados ahora.
        }
        int horasNuevas = gruposSeleccionadosIds.size();
        if (horasAsignadasPreviamente + horasNuevas > horasSemanales) {
            JOptionPane.showMessageDialog(this,
                "Error: La asignación excede el límite de " + horasSemanales + " horas semanales del profesor.\n" +
                "Horas actuales asignadas: " + horasAsignadasPreviamente + "\n" +
                "Nuevas horas a asignar: " + horasNuevas,
                "Límite de Horas Excedido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // --- FIN VALIDACIÓN ---

        if (profesorEnEdicion == null) { // Creando un nuevo profesor
            Profesor nuevoProfesor = new Profesor(nombre, materia, diasSeleccionados, horasSeleccionadas, horasSemanales);
            catalogo.addProfesor(nuevoProfesor);
            crearBloquesParaProfesor(nuevoProfesor, gruposSeleccionadosIds);
            JOptionPane.showMessageDialog(this, "Profesor '" + nombre + "' guardado y añadido al catálogo.", "Guardado Exitoso", JOptionPane.INFORMATION_MESSAGE);
        } else { // Editando un profesor existente
            profesorEnEdicion.setNombre(nombre);
            profesorEnEdicion.setMateriaAsignada(materia);
            profesorEnEdicion.setHorasSemanales(horasSemanales);
            profesorEnEdicion.setDiasDisponibles(diasSeleccionados);
            profesorEnEdicion.setHorasDisponibles(horasSeleccionadas);
            
            // Actualizar bloques: eliminar los viejos y crear los nuevos
            catalogo.removeBloquesByProfesorId(profesorEnEdicion.getId());
            crearBloquesParaProfesor(profesorEnEdicion, gruposSeleccionadosIds);
            JOptionPane.showMessageDialog(this, "Profesor '" + nombre + "' actualizado.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
        }

        // Refrescar la tabla y limpiar
        cargarDatosTabla(); 
        limpiarFormulario();

        // Cerrar el diálogo si está en uno
        if (parentDialog != null) {
            parentDialog.dispose();
        }
    }

    private void limpiarFormulario() {
        txtNombreProfesor.setText("");
        txtMateriaAsignada.setText("");
        txtHorasSemanales.setText("");
        for (JCheckBox check : checkDias) { check.setSelected(true); }
        for (JCheckBox check : checkHoras) { check.setSelected(true); }
        for (JCheckBox check : checkGrupos) { check.setSelected(false); }
        profesorEnEdicion = null; // Salir del modo edición
    }

    private void crearBloquesParaProfesor(Profesor profesor, List<String> grupoIds) {
        Salon salonEjemplo = catalogo.getTodosLosSalones().stream().findFirst().orElse(null);
        if (salonEjemplo == null) {
            // Opcional: Crear un salón por defecto si no existe
            salonEjemplo = new Salon("Salon General", 30);
            catalogo.addSalon(salonEjemplo);
        }

        for (String grupoId : grupoIds) {
            // Creamos un bloque de 1 hora, la hora de inicio es un placeholder
            BloqueHorario bloque = new BloqueHorario(
                LocalTime.of(7, 0), LocalTime.of(8, 0),
                profesor.getMateriaAsignada(),
                profesor.getId(),
                salonEjemplo.getId(),
                grupoId,
                true
            );
            // Asignar un día por defecto para que aparezca en el horario
            bloque.setDia("Lunes"); 
            catalogo.addBloqueHorario(bloque);
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
                    catalogo.removeBloquesByProfesorId(profesorAEliminar.getId()); // Eliminar sus bloques
                    cargarDatosTabla(); // Refrescar la tabla
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un profesor de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editarProfesorSeleccionado() {
        int filaSeleccionada = tablaProfesores.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un profesor de la tabla para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreProf = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        profesorEnEdicion = catalogo.findProfesorByName(nombreProf).orElse(null);

        if (profesorEnEdicion != null) {
            // Cargar datos en el formulario
            txtNombreProfesor.setText(profesorEnEdicion.getNombre());
            txtMateriaAsignada.setText(profesorEnEdicion.getMateriaAsignada());
            txtHorasSemanales.setText(String.valueOf(profesorEnEdicion.getHorasSemanales()));

            // Marcar disponibilidad de días y horas
            List<String> diasDisp = profesorEnEdicion.getDiasDisponibles();
            for (JCheckBox check : checkDias) {
                check.setSelected(diasDisp.contains(check.getText()));
            }
            List<String> horasDisp = profesorEnEdicion.getHorasDisponibles();
            for (JCheckBox check : checkHoras) {
                check.setSelected(horasDisp.contains(check.getText()));
            }

            // Marcar grupos asignados
            List<String> gruposAsignadosIds = catalogo.getBloquesByProfesorId(profesorEnEdicion.getId())
                .stream().map(BloqueHorario::getGrupoId).distinct().collect(Collectors.toList());
            for (JCheckBox check : checkGrupos) {
                String grupoId = (String) check.getClientProperty("grupoId");
                check.setSelected(gruposAsignadosIds.contains(grupoId));
            }
        }
    }

    public void setParentDialog(JDialog dialog) {
        this.parentDialog = dialog;
    }
}