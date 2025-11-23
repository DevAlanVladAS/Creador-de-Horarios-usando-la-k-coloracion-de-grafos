package src;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vista de horario por grupo. Soporta drag & drop y mantiene el panel de "sin asignar" fijo.
 */
public class PanelHorario extends JPanel implements GestorHorarios.HorarioChangeListener {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    private final LocalTime[] HORAS_DIA = PlantillaHoraria.BLOQUES_ESTANDAR.toArray(new LocalTime[0]);
    private final JPanel gridPanel;
    private final PanelSinAsignar panelSinAsignar;
    private final List<CeldaHorario> celdas = new ArrayList<>();
    private final DropTargetListener dropListenerCelda;

    private final GestorHorarios gestor;
    private final String grupoId;

    private boolean refrescando = false;

    public PanelHorario(String grupoId) {
        this.grupoId = grupoId;
        this.gestor = GestorHorarios.getInstance();

        setLayout(new BorderLayout(10, 10));

        gridPanel = new JPanel(new GridLayout(HORAS_DIA.length + 1, DIAS_SEMANA.length + 1));
        gridPanel.setBackground(Color.WHITE);

        dropListenerCelda = new CeldaDropListener();
        panelSinAsignar = new PanelSinAsignar(gestor);

        construirUI();
        gestor.addListener(this);
        refrescarVista();
    }

    private void construirUI() {
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
                celdas.add(celda);
                gridPanel.add(celda);
            }
        }

        JScrollPane scrollGrid = new JScrollPane(gridPanel);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollGrid, BorderLayout.CENTER);

        JScrollPane scrollSinAsignar = new JScrollPane(panelSinAsignar);
        scrollSinAsignar.setBorder(BorderFactory.createEmptyBorder());
        scrollSinAsignar.setPreferredSize(new Dimension(260, 0));
        scrollSinAsignar.setMinimumSize(new Dimension(260, 200));
        scrollSinAsignar.getVerticalScrollBar().setUnitIncrement(10);

        JPanel contenedor = new JPanel(new BorderLayout(5, 5));
        contenedor.setBorder(BorderFactory.createTitledBorder("Bloques sin asignar"));
        contenedor.add(scrollSinAsignar, BorderLayout.CENTER);
        add(contenedor, BorderLayout.EAST);
    }

    @Override
    public void onBloquesChanged(String grupoIdAfectado, GestorHorarios.TipoCambio tipoCambio, BloqueHorario bloqueAfectado) {
        if (grupoIdAfectado == null || grupoIdAfectado.equals(this.grupoId)) {
            SwingUtilities.invokeLater(this::refrescarVista);
        }
    }

    private void refrescarVista() {
        if (refrescando) return;

        try {
            refrescando = true;

            celdas.forEach(CeldaHorario::reset);
            panelSinAsignar.resetContenido();

            List<BloqueHorario> bloques = gestor.getBloquesGrupo(grupoId);
            Set<String> asignados = new HashSet<>();

            for (BloqueHorario bloque : bloques) {
                LocalTime horaInicio = bloque.getHoraInicio();
                LocalTime horaNormalizada = horaInicio != null ? horaInicio.withSecond(0).withNano(0) : null;

                if (bloque.getDia() != null && horaNormalizada != null) {
                    CeldaHorario celda = getCelda(bloque.getDia(), horaNormalizada);
                    if (celda != null) {
                        BloquePanel existente = celda.obtenerBloquePanel();
                        if (existente == null || existente.getBloque().getId().equals(bloque.getId())) {
                            BloquePanel panel = new BloquePanel(bloque);
                            celda.colocarBloque(panel);
                            asignados.add(bloque.getId());
                            continue;
                        }
                    }
                }
                if (!asignados.contains(bloque.getId())) {
                    agregarBloqueSinAsignar(bloque);
                }
            }

            actualizarMerges();
            panelSinAsignar.actualizarEstadoVacio();
            revalidate();
            repaint();

        } finally {
            refrescando = false;
        }
    }

    private CeldaHorario getCelda(String dia, LocalTime hora) {
        for (CeldaHorario celda : celdas) {
            if (celda.dia.equalsIgnoreCase(dia) && celda.hora.equals(hora)) {
                return celda;
            }
        }
        return null;
    }

    private void agregarBloqueSinAsignar(BloqueHorario bloque) {
        BloquePanel panel = new BloquePanel(bloque);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelSinAsignar.addBloquePanel(panel);
    }

    private boolean esDisponibleParaProfesor(String profesorId, String dia, LocalTime hora) {
        if (profesorId == null) return true;

        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        Profesor profesor = catalogo.obtenerProfesorPorId(profesorId);
        if (profesor == null) return true;

        List<String> diasDisponibles = profesor.getDiasDisponibles();
        if (diasDisponibles != null && !diasDisponibles.isEmpty()) {
            boolean diaEncontrado = diasDisponibles.stream()
                    .anyMatch(d -> d.equalsIgnoreCase(dia));
            if (!diaEncontrado) {
                return false;
            }
        }

        String horaFormateada = hora.format(DateTimeFormatter.ofPattern("H:mm"));
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
        if (a == null || b == null) return false;
        BloqueHorario bloqueA = a.getBloque();
        BloqueHorario bloqueB = b.getBloque();
        if (bloqueA == null || bloqueB == null) return false;

        boolean mismaMateria = bloqueA.getMateria() != null && bloqueA.getMateria().equalsIgnoreCase(bloqueB.getMateria());
        boolean mismoProfesor = bloqueA.getProfesorId() != null && bloqueA.getProfesorId().equals(bloqueB.getProfesorId());
        return mismaMateria && mismoProfesor;
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

    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }

    public void dispose() {
        gestor.removeListener(this);
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

        public BloquePanel obtenerBloquePanel() {
            if (getComponentCount() == 0) return null;
            Component comp = getComponent(0);
            return (comp instanceof BloquePanel) ? (BloquePanel) comp : null;
        }

        public String getDia() {
            return dia;
        }

        public LocalTime getHora() {
            return hora;
        }

        public void colocarBloque(BloquePanel panel) {
            BloquePanel ocupanteActual = obtenerBloquePanel();
            if (ocupanteActual != null && ocupanteActual != panel) {
                ocupanteActual.getBloque().setDia(null);
                if (panelSinAsignarRef != null) {
                    panelSinAsignarRef.addBloquePanel(ocupanteActual);
                }
            }
            Container parent = panel.getParent();
            if (parent != null && parent != this) {
                parent.remove(panel);
                parent.revalidate();
                parent.repaint();
            }
            removeAll();
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
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

    protected class CeldaDropListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            Component comp = dtde.getDropTargetContext().getComponent();
            if (comp instanceof CeldaHorario) {
                ((CeldaHorario) comp).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(78, 115, 223)));
            }
        }

        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}

        @Override
        public void dragExit(DropTargetEvent dte) {
            Component comp = dte.getDropTargetContext().getComponent();
            if (comp instanceof CeldaHorario) {
                ((CeldaHorario) comp).actualizarBorde(false, false);
            }
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            Component comp = dtde.getDropTargetContext().getComponent();
            if (!(comp instanceof CeldaHorario)) {
                dtde.rejectDrop();
                return;
            }

            CeldaHorario celda = (CeldaHorario) comp;
            boolean aceptado = false;

            try {
                Transferable tr = dtde.getTransferable();
                if (!tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                    dtde.rejectDrop();
                    return;
                }

                BloqueHorario bloqueTransferido = (BloqueHorario) tr.getTransferData(BloquePanel.DATA_FLAVOR);

                BloquePanel existente = celda.obtenerBloquePanel();
                if (existente != null && existente.getBloque().getId().equals(bloqueTransferido.getId())) {
                    dtde.rejectDrop();
                    return;
                }

                if (!esDisponibleParaProfesor(bloqueTransferido.getProfesorId(), celda.dia, celda.hora)) {
                    dtde.rejectDrop();
                    JOptionPane.showMessageDialog(null,
                            "El profesor no está disponible en este día/hora.",
                            "Restricción de disponibilidad",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                aceptado = true;

                refrescando = true;
                gestor.actualizarPosicionBloque(bloqueTransferido, celda.dia, celda.hora);
                refrescando = false;

                SwingUtilities.invokeLater(PanelHorario.this::refrescarVista);

                dtde.dropComplete(true);

            } catch (Exception e) {
                e.printStackTrace();
                if (!aceptado) {
                    dtde.rejectDrop();
                } else {
                    dtde.dropComplete(false);
                }
            } finally {
                refrescando = false;
                celda.actualizarBorde(false, false);
            }
        }
    }

    public static class PanelSinAsignar extends JPanel implements DropTargetListener {
        private final JLabel lblEmpty;
        private final GestorHorarios gestor;

        public PanelSinAsignar(GestorHorarios gestor) {
            this.gestor = gestor != null ? gestor : GestorHorarios.getInstance();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(248, 248, 248));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            lblEmpty = new JLabel("Arrastra aquí los bloques pendientes", SwingConstants.CENTER);
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
            Container parent = panel.getParent();
            if (parent != null && parent != this) {
                parent.remove(panel);
            }
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
                    gestor.actualizarPosicionBloque(bloqueTransferido, null, null);
                    dtde.dropComplete(true);
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dtde.rejectDrop();
            } finally {
                setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
        }
    }

    public static PanelHorario.PanelSinAsignar crearPanelSinAsignar() {
        return new PanelSinAsignar(GestorHorarios.getInstance());
    }
}