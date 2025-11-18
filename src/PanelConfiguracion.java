package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Panel centralizado para administrar profesores, grupos, salones, materias y asignaciones.
 */
public class PanelConfiguracion extends JPanel {

    private final CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
    private final boolean permitirGestionGrupos;
    private final boolean permitirAsignaciones;
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
    private final String[] HORAS_CLASE = {"7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00"};

    private JTable tablaProfesores;
    private DefaultTableModel modeloProfesores;
    private JTextField txtNombreProfesor;
    private JComboBox<Materia> cmbMateriaProfesor;
    private JSpinner spHorasProfesor;
    private JCheckBox[] checkDias;
    private JCheckBox[] checkHoras;
    private Profesor profesorEnEdicion;

    private JTable tablaGrupos;
    private DefaultTableModel modeloGrupos;
    private JTextField txtNombreGrupo;
    private JSpinner spGradoGrupo;

    private JTable tablaSalones;
    private DefaultTableModel modeloSalones;
    private JTextField txtNombreSalon;
    private JSpinner spCapacidadSalon;

    private JTable tablaMaterias;
    private DefaultTableModel modeloMaterias;
    private JTextField txtNombreMateria;
    private JSpinner spHorasMateria;

    private JTable tablaAsignaciones;
    private DefaultTableModel modeloAsignaciones;
    private JComboBox<GrupoEstudiantes> cmbGrupoAsignacion;
    private JComboBox<Materia> cmbMateriaAsignacion;
    private JComboBox<Profesor> cmbProfesorAsignacion;
    private JComboBox<Salon> cmbSalonAsignacion;
    private JSpinner spHorasAsignacion;
    private AsignacionAcademica asignacionEnEdicion;
    private JCheckBox chkMateriaLibre;

    private Materia materiaEnEdicion;

    private JDialog parentDialog;

    public PanelConfiguracion() {
        this(true, true);
    }

    public PanelConfiguracion(boolean permitirGestionGrupos, boolean permitirAsignaciones) {
        this.permitirGestionGrupos = permitirGestionGrupos;
        this.permitirAsignaciones = permitirAsignaciones;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Catálogo de recursos académicos", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(titulo, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profesores", crearPanelProfesores());
        tabs.addTab("Grupos", crearPanelGrupos());
        tabs.addTab("Salones", crearPanelSalones());
        tabs.addTab("Materias", crearPanelMaterias());
        tabs.addTab("Asignaciones", crearPanelAsignaciones());

        add(tabs, BorderLayout.CENTER);

        recargarDatos();
    }

    private JPanel crearPanelSoloLectura(String mensaje) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel label = new JLabel("<html>" + mensaje + "</html>", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void setParentDialog(JDialog parentDialog) {
        this.parentDialog = parentDialog;
    }

    // ----------------------------------------------------
    // PROFESORES
    // ----------------------------------------------------

    private JPanel crearPanelProfesores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setLeftComponent(crearListadoProfesores());
        split.setRightComponent(crearFormularioProfesores());
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearListadoProfesores() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Profesores registrados"));

        String[] columnas = {"Nombre", "Materia", "Horas/semana"};
        modeloProfesores = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaProfesores = new JTable(modeloProfesores);
        tablaProfesores.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(tablaProfesores), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");

        btnEditar.addActionListener(e -> editarProfesorSeleccionado());
        btnEliminar.addActionListener(e -> eliminarProfesorSeleccionado());

        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        panel.add(acciones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearFormularioProfesores() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo profesor"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        txtNombreProfesor = new JTextField(18);
        cmbMateriaProfesor = new JComboBox<>();
        cmbMateriaProfesor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Materia) {
                    setText(((Materia) value).getNombre());
                } else {
                    setText("Sin materia fija");
                }
                return this;
            }
        });
        spHorasProfesor = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));

        checkDias = new JCheckBox[DIAS_SEMANA.length];
        checkHoras = new JCheckBox[HORAS_CLASE.length];

        panel.add(crearField("Nombre completo", txtNombreProfesor));
        panel.add(crearField("Materia", cmbMateriaProfesor));
        panel.add(crearField("Horas por semana", spHorasProfesor));

        panel.add(crearPanelChecks("Días disponibles", DIAS_SEMANA, checkDias));
        panel.add(crearPanelChecks("Horario disponible", HORAS_CLASE, checkHoras));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnPreferencias = new JButton("Disponibilidad completa");
        btnPreferencias.addActionListener(e -> seleccionarTodos(checkDias, checkHoras));

        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.addActionListener(e -> guardarProfesor());

        botones.add(btnPreferencias);
        botones.add(btnGuardar);
        panel.add(botones);

        return panel;
    }

    private void guardarProfesor() {
        String nombre = txtNombreProfesor.getText().trim();
        Materia materia = (Materia) cmbMateriaProfesor.getSelectedItem();
        int horas = (Integer) spHorasProfesor.getValue();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del profesor.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> dias = obtenerSeleccion(checkDias);
        List<String> horasSeleccionadas = obtenerSeleccion(checkHoras);
        String materiaNombre = materia != null ? materia.getNombre() : null;

        if (profesorEnEdicion == null) {
            Profesor profesor = new Profesor(nombre, materiaNombre, dias, horasSeleccionadas, horas);
            catalogo.addProfesor(profesor);
        } else {
            profesorEnEdicion.setNombre(nombre);
            profesorEnEdicion.setMateriaAsignada(materiaNombre);
            profesorEnEdicion.setDiasDisponibles(dias);
            profesorEnEdicion.setHorasDisponibles(horasSeleccionadas);
            profesorEnEdicion.setHorasSemanales(horas);
            profesorEnEdicion = null;
        }

        limpiarFormularioProfesor();
        cargarProfesoresEnTabla();
        refrescarCombosAsignaciones();
    }

    private void eliminarProfesorSeleccionado() {
        int fila = tablaProfesores.getSelectedRow();
        if (fila < 0) return;
        String nombre = (String) modeloProfesores.getValueAt(fila, 0);
        Optional<Profesor> profesor = catalogo.getTodosLosProfesores().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst();
        profesor.ifPresent(p -> catalogo.removeProfesor(p.getId()));
        cargarProfesoresEnTabla();
        refrescarCombosAsignaciones();
    }

    private void editarProfesorSeleccionado() {
        int fila = tablaProfesores.getSelectedRow();
        if (fila < 0) return;
        String nombre = (String) modeloProfesores.getValueAt(fila, 0);
        profesorEnEdicion = catalogo.getTodosLosProfesores().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);
        if (profesorEnEdicion == null) {
            return;
        }
        txtNombreProfesor.setText(profesorEnEdicion.getNombre());
        seleccionarMateriaEnCombo(cmbMateriaProfesor, profesorEnEdicion.getMateriaAsignada());
        spHorasProfesor.setValue(profesorEnEdicion.getHorasSemanales());
        marcarSeleccion(checkDias, profesorEnEdicion.getDiasDisponibles());
        marcarSeleccion(checkHoras, profesorEnEdicion.getHorasDisponibles());
    }

    private void limpiarFormularioProfesor() {
        txtNombreProfesor.setText("");
        if (cmbMateriaProfesor.getItemCount() > 0) {
            cmbMateriaProfesor.setSelectedIndex(0);
        }
        spHorasProfesor.setValue(5);
        seleccionarTodos(checkDias, true);
        seleccionarTodos(checkHoras, true);
    }

    // ----------------------------------------------------
    // GRUPOS
    // ----------------------------------------------------

    private JPanel crearPanelGrupos() {
        if (!permitirGestionGrupos) {
            return crearPanelSoloLectura("Los grupos se generan al iniciar un proyecto. Usa el configurador de 'Nuevo proyecto' para definirlos.");
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Grupos escolares"));

        modeloGrupos = new DefaultTableModel(new String[]{"Grado", "Nombre del grupo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaGrupos = new JTable(modeloGrupos);
        tablaGrupos.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaGrupos.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(tablaGrupos), BorderLayout.CENTER);

        JPanel aviso = new JPanel(new BorderLayout());
        JLabel lblInfo = new JLabel("<html>Los grupos solo pueden editarse desde la configuración del proyecto actual.</html>");
        lblInfo.setHorizontalAlignment(SwingConstants.LEFT);
        aviso.add(lblInfo, BorderLayout.CENTER);
        panel.add(aviso, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel crearPanelSalones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Salones de clase"));

        modeloSalones = new DefaultTableModel(new String[]{"Nombre", "Capacidad"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaSalones = new JTable(modeloSalones);
        panel.add(new JScrollPane(tablaSalones), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtNombreSalon = new JTextField(12);
        spCapacidadSalon = new JSpinner(new SpinnerNumberModel(30, 5, 60, 1));

        JButton btnAgregar = new JButton("Agregar salón");
        JButton btnEliminar = new JButton("Eliminar seleccionado");

        btnAgregar.addActionListener(e -> guardarSalon());
        btnEliminar.addActionListener(e -> eliminarSalonSeleccionado());

        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombreSalon);
        formulario.add(new JLabel("Capacidad:"));
        formulario.add(spCapacidadSalon);
        formulario.add(btnAgregar);
        formulario.add(btnEliminar);

        panel.add(formulario, BorderLayout.SOUTH);
        return panel;
    }

    private void guardarSalon() {
        String nombre = txtNombreSalon.getText().trim();
        int capacidad = (Integer) spCapacidadSalon.getValue();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para el salón.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        catalogo.addSalon(new Salon(nombre, capacidad));
        txtNombreSalon.setText("");
        spCapacidadSalon.setValue(30);
        cargarSalonesEnTabla();
        refrescarCombosAsignaciones();
    }

    private void eliminarSalonSeleccionado() {
        int fila = tablaSalones.getSelectedRow();
        if (fila < 0) return;
        String nombre = (String) modeloSalones.getValueAt(fila, 0);
        catalogo.findSalonByName(nombre).ifPresent(s -> catalogo.removeSalon(s.getId()));
        cargarSalonesEnTabla();
        refrescarCombosAsignaciones();
    }

    // ----------------------------------------------------
    // MATERIAS
    // ----------------------------------------------------

    private JPanel crearPanelMaterias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Materias disponibles"));

        modeloMaterias = new DefaultTableModel(new String[]{"Nombre", "Horas sugeridas"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaMaterias = new JTable(modeloMaterias);
        panel.add(new JScrollPane(tablaMaterias), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtNombreMateria = new JTextField(15);
        spHorasMateria = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        JButton btnGuardar = new JButton("Guardar / Actualizar");
        JButton btnEditar = new JButton("Editar seleccionada");
        JButton btnEliminar = new JButton("Eliminar seleccionada");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> guardarOModificarMateria());
        btnEditar.addActionListener(e -> editarMateriaSeleccionada());
        btnEliminar.addActionListener(e -> eliminarMateriaSeleccionada());
        btnLimpiar.addActionListener(e -> limpiarFormularioMateria());

        formulario.add(new JLabel("Materia:"));
        formulario.add(txtNombreMateria);
        formulario.add(new JLabel("Horas sugeridas:"));
        formulario.add(spHorasMateria);
        formulario.add(btnGuardar);
        formulario.add(btnEditar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        panel.add(formulario, BorderLayout.SOUTH);
        return panel;
    }

    private void guardarOModificarMateria() {
        String nombre = txtNombreMateria.getText().trim();
        int horas = (Integer) spHorasMateria.getValue();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre de la materia.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (materiaEnEdicion == null) {
            Materia existente = catalogo.findMateriaByName(nombre).orElse(null);
            if (existente != null) {
                catalogo.actualizarHorasMateria(existente.getId(), horas);
            } else {
                Materia nueva = new Materia(nombre, horas);
                catalogo.addMateria(nueva);
            }
        } else {
            catalogo.actualizarHorasMateria(materiaEnEdicion.getId(), horas);
        }
        limpiarFormularioMateria();
        cargarMateriasEnTabla();
        cargarMateriasEnCombos();
    }

    private void eliminarMateriaSeleccionada() {
        int fila = tablaMaterias.getSelectedRow();
        if (fila < 0) return;
        String nombre = (String) modeloMaterias.getValueAt(fila, 0);
        catalogo.findMateriaByName(nombre).ifPresent(m -> catalogo.removeMateria(m.getId()));
        if (materiaEnEdicion != null && materiaEnEdicion.getNombre().equalsIgnoreCase(nombre)) {
            limpiarFormularioMateria();
        }
        cargarMateriasEnTabla();
        cargarMateriasEnCombos();
    }

    private void editarMateriaSeleccionada() {
        int fila = tablaMaterias.getSelectedRow();
        if (fila < 0) return;
        String nombre = (String) modeloMaterias.getValueAt(fila, 0);
        materiaEnEdicion = catalogo.findMateriaByName(nombre).orElse(null);
        if (materiaEnEdicion != null) {
            txtNombreMateria.setText(materiaEnEdicion.getNombre());
            txtNombreMateria.setEnabled(false);
            spHorasMateria.setValue(materiaEnEdicion.getHorasSugeridas());
        }
    }

    private void limpiarFormularioMateria() {
        materiaEnEdicion = null;
        txtNombreMateria.setText("");
        txtNombreMateria.setEnabled(true);
        spHorasMateria.setValue(3);
    }

    // ----------------------------------------------------
    // ASIGNACIONES
    // ----------------------------------------------------

    private JPanel crearPanelAsignaciones() {
        if (!permitirAsignaciones) {
            return crearPanelSoloLectura("Configura un proyecto y crea al menos un grupo para comenzar a registrar asignaciones.");
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        modeloAsignaciones = new DefaultTableModel(new String[]{"Grupo", "Materia", "Profesor", "Horas", "Salón", "ID"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaAsignaciones = new JTable(modeloAsignaciones);
        tablaAsignaciones.getTableHeader().setReorderingAllowed(false);
        tablaAsignaciones.removeColumn(tablaAsignaciones.getColumnModel().getColumn(5));

        panel.add(new JScrollPane(tablaAsignaciones), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Crear o actualizar asignación"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        chkMateriaLibre = new JCheckBox("Permitir materia distinta al profesor");
        chkMateriaLibre.addActionListener(e -> actualizarMateriaPorProfesor());

        cmbGrupoAsignacion = new JComboBox<>();
        cmbMateriaAsignacion = new JComboBox<>();
        cmbProfesorAsignacion = new JComboBox<>();
        cmbSalonAsignacion = new JComboBox<>();
        spHorasAsignacion = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        gbc.gridx = 0; gbc.gridy = 0; formulario.add(new JLabel("Grupo:"), gbc);
        gbc.gridx = 1; formulario.add(cmbGrupoAsignacion, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formulario.add(new JLabel("Profesor:"), gbc);
        gbc.gridx = 1; formulario.add(cmbProfesorAsignacion, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formulario.add(new JLabel("Materia:"), gbc);
        gbc.gridx = 1; formulario.add(cmbMateriaAsignacion, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formulario.add(new JLabel("Horas:"), gbc);
        gbc.gridx = 1; formulario.add(spHorasAsignacion, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formulario.add(new JLabel("Salón:"), gbc);
        gbc.gridx = 1; formulario.add(cmbSalonAsignacion, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; formulario.add(chkMateriaLibre, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar asignación");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");

        btnGuardar.addActionListener(e -> guardarAsignacion());
        btnEditar.addActionListener(e -> editarAsignacionSeleccionada());
        btnEliminar.addActionListener(e -> eliminarAsignacionSeleccionada());

        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnGuardar);

        gbc.gridy = 6;
        formulario.add(acciones, gbc);

        panel.add(formulario, BorderLayout.SOUTH);
        return panel;
    }

    private void guardarAsignacion() {
        if (!permitirAsignaciones || cmbGrupoAsignacion == null) {
            return;
        }

        GrupoEstudiantes grupo = (GrupoEstudiantes) cmbGrupoAsignacion.getSelectedItem();
        Profesor profesor = (Profesor) cmbProfesorAsignacion.getSelectedItem();
        Materia materia = (Materia) cmbMateriaAsignacion.getSelectedItem();
        Salon salon = (Salon) cmbSalonAsignacion.getSelectedItem();
        int horas = spHorasAsignacion != null ? (Integer) spHorasAsignacion.getValue() : 0;

        if (grupo == null || profesor == null || materia == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un grupo, profesor y materia para registrar la asignación.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (horas <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Las horas semanales deben ser mayores a cero.",
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
            if (spHorasAsignacion != null) {
                spHorasAsignacion.setValue(1);
            }
            return;
        }

        if (asignacionEnEdicion != null) {
            catalogo.removeAsignacion(asignacionEnEdicion.getId());
        }

        AsignacionAcademica asignacion = new AsignacionAcademica(
                grupo.getId(),
                profesor.getId(),
                materia.getId(),
                salon != null ? salon.getId() : null,
                horas
        );
        catalogo.addAsignacionAcademica(asignacion);

        asignacionEnEdicion = null;
        cargarAsignacionesEnTabla();
        refrescarCombosAsignaciones();
        limpiarFormularioAsignacion();

        JOptionPane.showMessageDialog(this,
                "Asignación guardada correctamente.",
                "Asignaciones",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void editarAsignacionSeleccionada() {
        if (!permitirAsignaciones || tablaAsignaciones == null) {
            return;
        }
        String id = obtenerIdAsignacionSeleccionada();
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una asignación en la tabla para editarla.",
                    "Asignaciones",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        asignacionEnEdicion = buscarAsignacionPorId(id);
        if (asignacionEnEdicion == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró la asignación seleccionada.",
                    "Asignaciones",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        seleccionarElemento(cmbGrupoAsignacion, asignacionEnEdicion.getGrupoId());
        seleccionarElemento(cmbProfesorAsignacion, asignacionEnEdicion.getProfesorId());
        if (chkMateriaLibre != null) {
            chkMateriaLibre.setSelected(true);
        }
        if (cmbMateriaAsignacion != null) {
            cmbMateriaAsignacion.setEnabled(true);
            seleccionarElemento(cmbMateriaAsignacion, asignacionEnEdicion.getMateriaId());
        }

        if (asignacionEnEdicion.getSalonId() != null) {
            seleccionarElemento(cmbSalonAsignacion, asignacionEnEdicion.getSalonId());
        } else if (cmbSalonAsignacion.getItemCount() > 0) {
            cmbSalonAsignacion.setSelectedIndex(0);
        }
        if (spHorasAsignacion != null) {
            spHorasAsignacion.setValue(asignacionEnEdicion.getHorasSemanales());
        }
    }

    private void eliminarAsignacionSeleccionada() {
        if (!permitirAsignaciones || tablaAsignaciones == null) {
            return;
        }
        String id = obtenerIdAsignacionSeleccionada();
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una asignación para eliminar.",
                    "Asignaciones",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar la asignación seleccionada?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        catalogo.removeAsignacion(id);
        if (asignacionEnEdicion != null && asignacionEnEdicion.getId().equals(id)) {
            asignacionEnEdicion = null;
            limpiarFormularioAsignacion();
        }
        cargarAsignacionesEnTabla();
        refrescarCombosAsignaciones();
    }

    private void cargarProfesoresEnTabla() {
        modeloProfesores.setRowCount(0);
        for (Profesor profesor : catalogo.getTodosLosProfesores()) {
            modeloProfesores.addRow(new Object[]{
                    profesor.getNombre(),
                    profesor.getMateriaAsignada(),
                    profesor.getHorasSemanales()
            });
        }
    }

    private void cargarGruposEnTabla() {
        if (modeloGrupos == null) return;
        modeloGrupos.setRowCount(0);
        for (GrupoEstudiantes grupo : catalogo.getTodosLosGrupos()) {
            modeloGrupos.addRow(new Object[]{grupo.getGrado(), grupo.getNombre()});
        }
    }

    private void cargarSalonesEnTabla() {
        modeloSalones.setRowCount(0);
        for (Salon salon : catalogo.getTodosLosSalones()) {
            modeloSalones.addRow(new Object[]{salon.getNombre(), salon.getCapacidad()});
        }
    }

    private void cargarMateriasEnTabla() {
        modeloMaterias.setRowCount(0);
        for (Materia materia : catalogo.getTodasLasMaterias()) {
            modeloMaterias.addRow(new Object[]{materia.getNombre(), materia.getHorasSugeridas()});
        }
    }

    private void cargarMateriasEnCombos() {
        if (cmbMateriaProfesor != null) {
            cmbMateriaProfesor.removeAllItems();
            cmbMateriaProfesor.addItem(null);
            for (Materia materia : catalogo.getTodasLasMaterias()) {
                cmbMateriaProfesor.addItem(materia);
            }
        }
        if (cmbMateriaAsignacion != null) {
            cmbMateriaAsignacion.removeAllItems();
            for (Materia materia : catalogo.getTodasLasMaterias()) {
                cmbMateriaAsignacion.addItem(materia);
            }
        }
        actualizarMateriaPorProfesor();
    }

    private void refrescarCombosAsignaciones() {
        if (cmbGrupoAsignacion == null || cmbMateriaAsignacion == null || cmbProfesorAsignacion == null || cmbSalonAsignacion == null) {
            return;
        }
        cmbGrupoAsignacion.removeAllItems();
        for (GrupoEstudiantes grupo : catalogo.getTodosLosGrupos()) {
            cmbGrupoAsignacion.addItem(grupo);
        }

        cmbProfesorAsignacion.removeAllItems();
        for (Profesor profesor : catalogo.getTodosLosProfesores()) {
            cmbProfesorAsignacion.addItem(profesor);
        }

        cmbSalonAsignacion.removeAllItems();
        cmbSalonAsignacion.addItem(null);
        for (Salon salon : catalogo.getTodosLosSalones()) {
            cmbSalonAsignacion.addItem(salon);
        }
        actualizarMateriaPorProfesor();
    }

    private void cargarAsignacionesEnTabla() {
        if (modeloAsignaciones == null) return;
        modeloAsignaciones.setRowCount(0);
        for (AsignacionAcademica asignacion : catalogo.getAsignaciones()) {
            GrupoEstudiantes grupo = catalogo.obtenerGrupoPorId(asignacion.getGrupoId());
            Materia materia = catalogo.obtenerMateriaPorId(asignacion.getMateriaId());
            Profesor profesor = catalogo.obtenerProfesorPorId(asignacion.getProfesorId());
            Salon salon = asignacion.getSalonId() != null ? catalogo.obtenerSalonPorId(asignacion.getSalonId()) : null;

            modeloAsignaciones.addRow(new Object[]{
                    grupo != null ? grupo.toString() : "(Grupo eliminado)",
                    materia != null ? materia.getNombre() : "(Materia eliminada)",
                    profesor != null ? profesor.getNombre() : "(Profesor eliminado)",
                    asignacion.getHorasSemanales(),
                    salon != null ? salon.getNombre() : "Libre",
                    asignacion.getId()
            });
        }
    }

    private void actualizarMateriaPorProfesor() {
        if (cmbMateriaAsignacion == null || cmbProfesorAsignacion == null) {
            return;
        }
        Profesor profesor = (Profesor) cmbProfesorAsignacion.getSelectedItem();
        if (profesor == null) {
            cmbMateriaAsignacion.setEnabled(true);
            return;
        }
        if (chkMateriaLibre != null && chkMateriaLibre.isSelected()) {
            cmbMateriaAsignacion.setEnabled(true);
            return;
        }
        String materiaNombre = profesor.getMateriaAsignada();
        if (materiaNombre == null || materiaNombre.isBlank()) {
            cmbMateriaAsignacion.setEnabled(true);
            return;
        }
        Materia materia = catalogo.findMateriaByName(materiaNombre).orElse(null);
        if (materia != null) {
            seleccionarElemento(cmbMateriaAsignacion, materia.getId());
            cmbMateriaAsignacion.setEnabled(false);
        } else {
            cmbMateriaAsignacion.setEnabled(true);
        }
    }

    // ----------------------------------------------------
    // UTILIDADES
    // ----------------------------------------------------

    private JPanel crearField(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(etiqueta + ":"));
        panel.add(componente);
        return panel;
    }

    private JPanel crearPanelChecks(String titulo, String[] valores, JCheckBox[] destino) {
        JPanel panel = new JPanel(new GridLayout(0, 3, 5, 2));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        for (int i = 0; i < valores.length; i++) {
            destino[i] = new JCheckBox(valores[i], true);
            panel.add(destino[i]);
        }
        return panel;
    }

    private List<String> obtenerSeleccion(JCheckBox[] checks) {
        return Arrays.stream(checks)
                .filter(JCheckBox::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.toList());
    }

    private void seleccionarTodos(JCheckBox[] checks, boolean estado) {
        Arrays.stream(checks).forEach(c -> c.setSelected(estado));
    }

    private void seleccionarTodos(JCheckBox[] dias, JCheckBox[] horas) {
        seleccionarTodos(dias, true);
        seleccionarTodos(horas, true);
    }

    private void marcarSeleccion(JCheckBox[] checks, List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            // Si la lista está vacía, significa que ninguno está seleccionado, no todos.
            seleccionarTodos(checks, false);
        }
        for (JCheckBox check : checks) {
            check.setSelected(valores.contains(check.getText()));
        }
    }

    private void seleccionarMateriaEnCombo(JComboBox<Materia> combo, String nombreMateria) {
        if (nombreMateria == null || nombreMateria.isBlank()) {
            if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Materia materia = combo.getItemAt(i);
            if (materia != null && materia.getNombre().equalsIgnoreCase(nombreMateria)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarElemento(JComboBox<?> combo, String id) {
        if (combo == null || combo.getItemCount() == 0) {
            return;
        }
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object elemento = combo.getItemAt(i);
            if (elemento instanceof GrupoEstudiantes && ((GrupoEstudiantes) elemento).getId().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
            if (elemento instanceof Materia && ((Materia) elemento).getId().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
            if (elemento instanceof Profesor && ((Profesor) elemento).getId().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
            if (elemento instanceof Salon && ((Salon) elemento).getId().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private void limpiarFormularioAsignacion() {
        if (tablaAsignaciones != null) {
            tablaAsignaciones.clearSelection();
        }
        asignacionEnEdicion = null;
        if (cmbGrupoAsignacion != null && cmbGrupoAsignacion.getItemCount() > 0) {
            cmbGrupoAsignacion.setSelectedIndex(0);
        }
        if (cmbProfesorAsignacion != null && cmbProfesorAsignacion.getItemCount() > 0) {
            cmbProfesorAsignacion.setSelectedIndex(0);
        }
        if (cmbMateriaAsignacion != null && cmbMateriaAsignacion.getItemCount() > 0) {
            cmbMateriaAsignacion.setSelectedIndex(0);
        }
        if (cmbSalonAsignacion != null && cmbSalonAsignacion.getItemCount() > 0) {
            cmbSalonAsignacion.setSelectedIndex(0);
        }
        if (spHorasAsignacion != null) {
            spHorasAsignacion.setValue(1);
        }
        if (chkMateriaLibre != null) {
            chkMateriaLibre.setSelected(false);
        }
        actualizarMateriaPorProfesor();
    }

    private String obtenerIdAsignacionSeleccionada() {
        if (tablaAsignaciones == null) {
            return null;
        }
        int fila = tablaAsignaciones.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int filaModelo = tablaAsignaciones.convertRowIndexToModel(fila);
        Object valor = modeloAsignaciones.getValueAt(filaModelo, 5);
        return valor != null ? valor.toString() : null;
    }

    private AsignacionAcademica buscarAsignacionPorId(String id) {
        if (id == null) {
            return null;
        }
        return catalogo.getAsignaciones().stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .orElse(null);
    }

    private void agregarCampoFormulario(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent componente) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(componente, gbc);
    }

    private void recargarDatos() {
        cargarProfesoresEnTabla();
        cargarGruposEnTabla();
        cargarSalonesEnTabla();
        cargarMateriasEnTabla();
        cargarMateriasEnCombos();
        cargarAsignacionesEnTabla();
        refrescarCombosAsignaciones();
    }
}
