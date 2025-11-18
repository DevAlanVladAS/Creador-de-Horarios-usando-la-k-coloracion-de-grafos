package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public PanelHorario() {
        setLayout(new BorderLayout(10, 10));

        gridPanel = new JPanel(new GridLayout(HORAS_DIA.length + 1, DIAS_SEMANA.length + 1));
        gridPanel.setBackground(Color.WHITE);

        gridPanel.add(new JLabel(""));
        for (String dia : DIAS_SEMANA) {
            gridPanel.add(crearCabeceraDia(dia));
        }

        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCabeceraHora(formatearHora(hora)));
            for (String dia : DIAS_SEMANA) {
                CeldaHorario celda = new CeldaHorario(dia, hora);
                celdas.add(celda);
                gridPanel.add(celda);
            }
        }

        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        panelSinAsignar = new PanelSinAsignar();
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

    public void cargarBloques(List<BloqueHorario> bloques) {
        limpiarCeldasHorario();
        panelSinAsignar.resetContenido();

        for (BloqueHorario bloque : bloques) {
            LocalTime horaInicio = bloque.getHoraInicio();
            LocalTime horaNormalizada = horaInicio != null ? horaInicio.withSecond(0).withNano(0) : null;

            if (bloque.getDia() != null && horaNormalizada != null) {
                CeldaHorario celda = getCelda(bloque.getDia(), horaNormalizada);
                if (celda != null) {
                    BloquePanel panel = new BloquePanel(bloque);
                    celda.colocarBloque(panel);
                    continue;
                }
            }

            agregarBloqueSinAsignar(bloque);
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

    private void actualizarMerges() {
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

    private class CeldaHorario extends JPanel implements DropTargetListener {
        private final String dia;
        private final LocalTime hora;

        CeldaHorario(String dia, LocalTime hora) {
            this.dia = dia;
            this.hora = hora;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            actualizarBorde(false, false);
            new DropTarget(this, DnDConstants.ACTION_MOVE, this, true);
        }

        void colocarBloque(BloquePanel panel) {
            BloquePanel ocupanteActual = obtenerBloquePanel();
            if (ocupanteActual != null && ocupanteActual != panel) {
                ocupanteActual.getBloque().setDia(null);
                panelSinAsignar.addBloquePanel(ocupanteActual);
            }
            removeAll();
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        BloquePanel obtenerBloquePanel() {
            if (getComponentCount() == 0) {
                return null;
            }
            Component comp = getComponent(0);
            return (comp instanceof BloquePanel) ? (BloquePanel) comp : null;
        }

        void reset() {
            removeAll();
            actualizarBorde(false, false);
            revalidate();
            repaint();
        }

        void actualizarBorde(boolean mergeTop, boolean mergeBottom) {
            int top = mergeTop ? 0 : 1;
            int bottom = mergeBottom ? 0 : 1;
            setBorder(BorderFactory.createMatteBorder(top, 1, bottom, 1, new Color(230, 230, 230)));
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(78, 115, 223)));
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {}

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {}

        @Override
        public void dragExit(DropTargetEvent dte) {
            actualizarBorde(false, false);
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
                    colocarBloque(bloquePanel);
                    bloqueTransferido.setDia(this.dia);
                    bloqueTransferido.setHoraInicio(this.hora);
                    bloqueTransferido.setHoraFin(this.hora.plus(PlantillaHoraria.DURACION_BLOQUE));
                    dtde.dropComplete(true);
                    actualizarMerges();
                    panelSinAsignar.actualizarEstadoVacio();
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                dtde.rejectDrop();
            } finally {
                actualizarBorde(false, false);
            }
        }
    }

    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }

    private class PanelSinAsignar extends JPanel implements DropTargetListener {
        private final JLabel lblEmpty;

        PanelSinAsignar() {
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
}
