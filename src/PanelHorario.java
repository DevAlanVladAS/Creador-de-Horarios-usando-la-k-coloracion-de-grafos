package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel que muestra la cuadrícula semanal y permite arrastrar BloquePanel entre celdas.
 */
public class PanelHorario extends JPanel {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime[] HORAS_DIA = PlantillaHoraria.BLOQUES_ESTANDAR.toArray(new LocalTime[0]);
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Mi\u00e9rcoles", "Jueves", "Viernes"};

    private final JPanel gridPanel;
    private final PanelSinAsignar panelSinAsignar;
    private final List<CeldaHorario> celdas = new ArrayList<>();
    private final DropTargetListener dropListenerCelda;
    private Runnable onBloquesActualizados;

    public PanelHorario() {
        setLayout(new BorderLayout(10, 10));

        gridPanel = new JPanel(new GridLayout(HORAS_DIA.length + 1, DIAS_SEMANA.length + 1));
        gridPanel.setBackground(Color.WHITE);

        dropListenerCelda = new CeldaDropListener();
        panelSinAsignar = new PanelSinAsignar();

        gridPanel.add(new JLabel(""));
        for (String dia : DIAS_SEMANA) {
            gridPanel.add(crearCabeceraDia(dia));
        }

        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCabeceraHora(formatearHora(hora)));
            for (String dia : DIAS_SEMANA) {
                CeldaHorario celda = new CeldaHorario(dia, hora);
                celda.setPanelSinAsignar(panelSinAsignar);
                celda.setDropListener(dropListenerCelda);
                celdas.add(celda); //
                gridPanel.add(celda);
            }
        }

        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        JScrollPane scrollSinAsignar = new JScrollPane(panelSinAsignar);
        scrollSinAsignar.setBorder(BorderFactory.createEmptyBorder());
        scrollSinAsignar.setPreferredSize(new Dimension(240, 0));

        JPanel contenedor = new JPanel(new BorderLayout(5, 5));
        contenedor.setBorder(BorderFactory.createTitledBorder("Bloques sin asignar"));
        contenedor.add(scrollSinAsignar, BorderLayout.CENTER);

        add(contenedor, BorderLayout.EAST);
    }

    private CeldaHorario getCelda(String dia, LocalTime hora) {
        for (CeldaHorario celda : celdas) {
            if (celda.dia.equalsIgnoreCase(dia) && celda.hora.equals(hora)) {
                return celda;
            }
        }
        return null;
    }

    public void cargarBloques(List<BloqueHorario> nuevosBloques) {
        List<BloquePanel> panelesActuales = getAllBloquePanels();
        List<String> idsNuevos = nuevosBloques.stream().map(BloqueHorario::getId).collect(Collectors.toList());
        List<String> idsActuales = panelesActuales.stream().map(p -> p.getBloque().getId()).collect(Collectors.toList());

        // 1. Eliminar paneles de bloques que ya no existen
        for (BloquePanel panelActual : panelesActuales) {
            if (!idsNuevos.contains(panelActual.getBloque().getId())) {
                Container parent = panelActual.getParent();
                if (parent != null) {
                    parent.remove(panelActual);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        }

        // 2. Agregar nuevos bloques que no están en el panel
        for (BloqueHorario nuevoBloque : nuevosBloques) {
            if (!idsActuales.contains(nuevoBloque.getId())) {
                // Si el bloque nuevo ya tiene una posición asignada, colócalo
                LocalTime horaInicio = nuevoBloque.getHoraInicio();
                LocalTime horaNormalizada = horaInicio != null ? horaInicio.withSecond(0).withNano(0) : null;

                if (nuevoBloque.getDia() != null && horaNormalizada != null) {
                    CeldaHorario celda = getCelda(nuevoBloque.getDia(), horaNormalizada);
                    if (celda != null) {
                        BloquePanel panel = new BloquePanel(nuevoBloque);
                        celda.colocarBloque(panel);
                        continue;
                    }
                }
                // Si no, agrégalo al panel de sin asignar
                agregarBloqueSinAsignar(nuevoBloque);
            }
            // Si el bloque ya existe, no hacemos nada para no alterar su posición actual.
            // Se podría agregar lógica para actualizar su contenido si fuera necesario.
        }

        actualizarMerges();
        panelSinAsignar.actualizarEstadoVacio();
        revalidate();
        repaint();
    }

    private void limpiarCeldasHorario() {
        for (CeldaHorario celda : celdas) {
            celda.reset();
        }
    }

    private void agregarBloqueSinAsignar(BloqueHorario bloque) {
        BloquePanel panel = new BloquePanel(bloque);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelSinAsignar.addBloquePanel(panel);
    }

    public List<BloquePanel> getAllBloquePanels() {
    List<BloquePanel> lista = new ArrayList<>();
    for (CeldaHorario celda : celdas) {
        if (celda.obtenerBloquePanel() != null) {
            lista.add(celda.obtenerBloquePanel());
        }
    }
    for (Component comp : panelSinAsignar.getComponents()) {
        if (comp instanceof BloquePanel) {
            lista.add((BloquePanel) comp);
        }
    }
    return lista;
}

    private JLabel crearCabeceraDia(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setOpaque(true);
        label.setBackground(new Color(78, 115, 223));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(60, 80, 140)));
        return label;
    }

    private JLabel crearCabeceraHora(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(223, 230, 251));
        label.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(200, 210, 240)));
        return label;
    }

    /**
     * Mueve un bloque (identificado por su objeto) a la celda correspondiente.
     * Usado por el animador.
     * @param bloque El bloque a mover.
     */
    public void moverBloqueACelda(BloqueHorario bloque) {
        if (bloque == null || bloque.getDia() == null || bloque.getHoraInicio() == null) return;

        CeldaHorario celdaDestino = getCelda(bloque.getDia(), bloque.getHoraInicio());
        if (celdaDestino == null) return;

        BloquePanel panel = findBloquePanelById(bloque.getId());
        if (panel == null) return;

        Container oldParent = panel.getParent();
        if (oldParent != null) {
            oldParent.remove(panel);
            oldParent.revalidate();
            oldParent.repaint();
        }
        celdaDestino.colocarBloque(panel);
    }

    private BloquePanel findBloquePanelById(String id) {
        for (CeldaHorario celda : celdas) {
            BloquePanel panel = celda.obtenerBloquePanel();
            if (panel != null && panel.getBloque().getId().equals(id)) {
                return panel;
            }
        }
        for (Component comp : panelSinAsignar.getComponents()) {
            if (comp instanceof BloquePanel) {
                BloquePanel panel = (BloquePanel) comp;
                if (panel.getBloque().getId().equals(id)) {
                    return panel;
                }
            }
        }
        return null;
    }

    private boolean esDisponibleParaProfesor(String profesorId, String dia, LocalTime hora) {
        if (profesorId == null) return true; // Si no hay profesor, no hay restricción

        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        Profesor profesor = catalogo.obtenerProfesorPorId(profesorId);
        if (profesor == null) return true; // Profesor no encontrado, no se puede validar

        // 1. Validar día disponible
        List<String> diasDisponibles = profesor.getDiasDisponibles();
        if (diasDisponibles != null && !diasDisponibles.isEmpty()) {
            // El nombre del día en DIAS_SEMANA puede tener tilde ("Miércoles")
            // y el de la celda no. Comparamos ignorando mayúsculas y tildes si es necesario.
            boolean diaEncontrado = diasDisponibles.stream().anyMatch(d -> d.equalsIgnoreCase(dia));
            if (!diaEncontrado) {
                return false;
            }
        }

        // 2. Validar hora disponible
        String horaFormateada = hora.format(DateTimeFormatter.ofPattern("H:mm")); // Formato como "7:00"
        List<String> horasDisponibles = profesor.getHorasDisponibles();
        if (horasDisponibles != null && !horasDisponibles.isEmpty()) {
            return horasDisponibles.contains(horaFormateada);
        }
        
        return true;
    }

    public void actualizarMerges() {
        for (CeldaHorario celda : celdas) {
            BloquePanel panel = celda.obtenerBloquePanel();
            if (panel != null) {
                panel.resetMergeState();
            }
            celda.actualizarBorde(false, false);
        }

        for (String dia : DIAS_SEMANA) {
            for (int i = 0; i < HORAS_DIA.length; i++) {
                CeldaHorario actual = getCelda(dia, HORAS_DIA[i]);
                if (actual == null) continue;
                BloquePanel panelActual = actual.obtenerBloquePanel();
                if (panelActual == null) continue;

                CeldaHorario arriba = (i > 0) ? getCelda(dia, HORAS_DIA[i - 1]) : null;
                CeldaHorario abajo = (i + 1 < HORAS_DIA.length) ? getCelda(dia, HORAS_DIA[i + 1]) : null;

                boolean mergeTop = arriba != null && debenUnirse(arriba.obtenerBloquePanel(), panelActual);
                boolean mergeBottom = abajo != null && debenUnirse(panelActual, abajo.obtenerBloquePanel());

                panelActual.setMergeState(mergeTop, mergeBottom);
                actual.actualizarBorde(mergeTop, mergeBottom);
            }
        }
    }

    private boolean debenUnirse(BloquePanel a, BloquePanel b) {
        if (a == null || b == null) {
            return false;
        }
        BloqueHorario bloqueA = a.getBloque();
        BloqueHorario bloqueB = b.getBloque();
        if (bloqueA == null || bloqueB == null) {
            return false;
        }
        boolean mismaMateria = bloqueA.getMateria() != null
                && bloqueA.getMateria().equalsIgnoreCase(bloqueB.getMateria());
        boolean mismoProfesor = bloqueA.getProfesorId() != null
                && bloqueA.getProfesorId().equals(bloqueB.getProfesorId());
        return mismaMateria && mismoProfesor;
    }

    protected static class CeldaHorario extends JPanel {
        protected final String dia;
        protected final LocalTime hora;
        protected PanelSinAsignar panelSinAsignarRef;

        public CeldaHorario(String dia, LocalTime hora) {
            this.dia = dia;
            this.hora = hora;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            actualizarBorde(false, false);
        }

        public void setPanelSinAsignar(PanelSinAsignar panel) {
            this.panelSinAsignarRef = panel;
        }

        public void setDropListener(DropTargetListener listener) {
            new DropTarget(this, DnDConstants.ACTION_MOVE, listener, true);
        }

        public String getDia() { return dia; }
        public LocalTime getHora() { return hora; }

        public void colocarBloque(BloquePanel panel) {
            BloquePanel ocupanteActual = obtenerBloquePanel();
            if (ocupanteActual != null && ocupanteActual != panel) {
                ocupanteActual.getBloque().setDia(null);
                if (panelSinAsignarRef != null) {
                    panelSinAsignarRef.addBloquePanel(ocupanteActual);
                }
            }
            removeAll();
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        public BloquePanel obtenerBloquePanel() {
            if (getComponentCount() == 0) {
                return null;
            }
            Component comp = getComponent(0);
            return (comp instanceof BloquePanel) ? (BloquePanel) comp : null;
        }

        public void reset() {
            removeAll();
            actualizarBorde(false, false);
            revalidate();
            repaint();
        }

        public void actualizarBorde(boolean mergeTop, boolean mergeBottom) {
            int top = mergeTop ? 0 : 1;
            int bottom = mergeBottom ? 0 : 1;
            setBorder(BorderFactory.createMatteBorder(top, 1, bottom, 1, new Color(230, 230, 230)));
        }
    }

    /**
     * Listener compartido para todas las celdas para manejar el Drop.
     */
    protected class CeldaDropListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            Component comp = dtde.getDropTargetContext().getComponent();
            if (comp instanceof CeldaHorario) {
                ((CeldaHorario) comp).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(78, 115, 223)));
            }
        }
        @Override
        public void dragOver(DropTargetDragEvent dtde) {}

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {}

        @Override
        public void dragExit(DropTargetEvent dte) {
            Component comp = dte.getDropTargetContext().getComponent();
            if (comp instanceof CeldaHorario) {
                // La actualización del borde se hará en el 'finally' del drop
                // o si el drop es rechazado. Para evitar flickering, lo manejamos
                // de forma centralizada.
                ((CeldaHorario) comp).actualizarBorde(false, false);
            }
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            Component comp = dtde.getDropTargetContext().getComponent();
            if (!(comp instanceof CeldaHorario)) { dtde.rejectDrop(); return; }
            CeldaHorario celda = (CeldaHorario) comp;
            boolean aceptado = false;
            try {
                Transferable tr = dtde.getTransferable();
                if (!tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                    dtde.rejectDrop();
                    return;
                }
                
                BloqueHorario bloqueTransferido = (BloqueHorario) tr.getTransferData(BloquePanel.DATA_FLAVOR);
                
                // Validar disponibilidad (día + hora) ANTES de acceptar
                if (!esDisponibleParaProfesor(bloqueTransferido.getProfesorId(), celda.dia, celda.hora)) {
                    dtde.rejectDrop();
                    String razon = "El profesor no está disponible en este día/hora.";
                    JOptionPane.showMessageDialog(null, razon,
                            "Restricción de disponibilidad", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                BloquePanel bloquePanel = findBloquePanelById(bloqueTransferido.getId());
                if (bloquePanel == null) {
                    dtde.rejectDrop();
                    return;
                }
                
                // AHORA aceptar después de validar todo
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                aceptado = true;
                
                Container oldParent = bloquePanel.getParent();
                if (oldParent != null) {
                    oldParent.remove(bloquePanel);
                    oldParent.revalidate();
                    oldParent.repaint();
                }
                celda.colocarBloque(bloquePanel);
                bloqueTransferido.setDia(celda.dia);
                bloqueTransferido.setHoraInicio(celda.hora);
                bloqueTransferido.setHoraFin(celda.hora.plus(PlantillaHoraria.DURACION_BLOQUE));
                dtde.dropComplete(true);
                actualizarMerges();
                panelSinAsignar.actualizarEstadoVacio();
                notificarCambios();
            } catch (Exception e) {
                // Solo llamar rejectDrop si no fue aceptado aún
                if (!aceptado) {
                    dtde.rejectDrop();
                } else {
                    // Si ya fue aceptado, solo hacer dropComplete(false)
                    dtde.dropComplete(false);
                }
            } finally {
                celda.actualizarBorde(false, false);
            }
        }
    }

    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }

    protected class PanelSinAsignar extends JPanel implements DropTargetListener {
        private final JLabel lblEmpty;

        public PanelSinAsignar() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(248, 248, 248));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            lblEmpty = new JLabel("Arrastra aqui los bloques pendientes", SwingConstants.CENTER);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblEmpty);
            new DropTarget(this, DnDConstants.ACTION_MOVE, this, true);
        }

        void resetContenido() {
            removeAll();
            add(lblEmpty);
            revalidate();
            repaint();
        }

        void addBloquePanel(BloquePanel panel) {
            if (lblEmpty.getParent() == this) {
                remove(lblEmpty);
            }
            add(panel);
            revalidate();
            repaint();
        }

        void actualizarEstadoVacio() {
            boolean hayBloques = false;
            for (Component comp : getComponents()) {
                if (comp instanceof BloquePanel) {
                    hayBloques = true;
                    break;
                }
            }
            lblEmpty.setVisible(!hayBloques);
            if (!hayBloques && lblEmpty.getParent() != this) {
                add(lblEmpty);
            }
            revalidate();
            repaint();
        }

        @Override public void dragEnter(DropTargetDragEvent dtde) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(78, 115, 223), 2),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        }
        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
        @Override public void dragExit(DropTargetEvent dte) {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable tr = dtde.getTransferable();
                if (tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    BloqueHorario bloqueTransferido = (BloqueHorario) tr.getTransferData(BloquePanel.DATA_FLAVOR);
                    BloquePanel bloquePanel = findBloquePanelById(bloqueTransferido.getId());
                    if (bloquePanel == null) {
                        dtde.rejectDrop();
                        return;
                    }
                    Container oldParent = bloquePanel.getParent();
                    if (oldParent != null) {
                        oldParent.remove(bloquePanel);
                        oldParent.revalidate();
                        oldParent.repaint();
                    }
                    bloqueTransferido.setDia(null);
                    addBloquePanel(bloquePanel);
                    actualizarEstadoVacio();
                    dtde.dropComplete(true);
                    actualizarMerges();
                    notificarCambios();
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                dtde.rejectDrop();
            } finally {
                setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
        }
    }

    public static PanelHorario.PanelSinAsignar crearPanelSinAsignar() {
        PanelHorario temp = new PanelHorario();
        return temp.new PanelSinAsignar();
    }

    public void setOnBloquesActualizados(Runnable listener) {
        this.onBloquesActualizados = listener;
    }

    private void notificarCambios() {
        if (onBloquesActualizados != null) {
            onBloquesActualizados.run();
        }
    }
}
