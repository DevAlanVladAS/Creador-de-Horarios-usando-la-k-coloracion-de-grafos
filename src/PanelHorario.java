package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel que muestra la cuadr��cula del horario de un Grupo.
 * Implementa Drag-and-Drop para los BloquePanel.
 */
public class PanelHorario extends JPanel {

    private String grupoId;
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime[] HORAS_DIA = {
        LocalTime.of(7, 0),
        LocalTime.of(8, 0),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        LocalTime.of(12, 0),
        LocalTime.of(13, 0),
        LocalTime.of(14, 0),
        LocalTime.of(15, 0)
    };
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "MiǸrcoles", "Jueves", "Viernes"};
    private final JPanel gridPanel;
    private final PanelSinAsignar panelSinAsignar;

    public PanelHorario() {
        this.grupoId = grupoId;
        setLayout(new BorderLayout(10, 10));
        
        gridPanel = new JPanel(new GridLayout(HORAS_DIA.length + 1, DIAS_SEMANA.length + 1));
        
        // 1. Esquina vac��a
        gridPanel.add(new JLabel("")); 
        
        // 2. Cabecera D��as
        for (String dia : DIAS_SEMANA) {
            gridPanel.add(crearCeldaCabecera(dia));
        }
        
        // 3. Cabecera Horas y Celdas de Horario
        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCeldaCabecera(formatearHora(hora))); // Cabecera de Hora
            for (String dia : DIAS_SEMANA) {
                gridPanel.add(new CeldaHorario(dia, hora, this)); 
            }
        }
        
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        panelSinAsignar = new PanelSinAsignar();
        JScrollPane scrollSinAsignar = new JScrollPane(panelSinAsignar);
        scrollSinAsignar.setBorder(BorderFactory.createEmptyBorder());
        scrollSinAsignar.setPreferredSize(new Dimension(240, 0));

        JPanel contenedorSinAsignar = new JPanel(new BorderLayout(5, 5));
        contenedorSinAsignar.setBorder(BorderFactory.createTitledBorder("Bloques sin asignar"));
        contenedorSinAsignar.add(scrollSinAsignar, BorderLayout.CENTER);

        add(contenedorSinAsignar, BorderLayout.EAST);
    }
    
    /**
     * Busca una celda espec��fica en la cuadr��cula por d��a y hora.
     * @param dia El d��a de la semana (e.g., "Lunes").
     * @param hora La hora en formato "HH:mm" (e.g., "07:00").
     * @return La CeldaHorario correspondiente, o null si no se encuentra.
     */
    private CeldaHorario getCelda(String dia, LocalTime hora) {
        if (dia == null || hora == null) {
            return null;
        }
        for (Component comp : gridPanel.getComponents()) {
            if (comp instanceof CeldaHorario) {
                CeldaHorario celda = (CeldaHorario) comp;
                if (celda.dia.equalsIgnoreCase(dia) && celda.hora.equals(hora)) {
                    return celda;
                }
            }
        }
        return null;
    }

    /**
     * Carga una lista de bloques en el panel del horario, colocǭndolos en sus celdas correspondientes.
     * Este mǸtodo reemplaza la simulaci��n y permite cargar datos reales.
     * @param bloques La lista de BloqueHorario a mostrar.
     */
    public void cargarBloques(List<BloqueHorario> bloques) {
        limpiarCeldasHorario();
        panelSinAsignar.resetContenido();

        for (BloqueHorario bloque : bloques) {
            LocalTime horaInicio = bloque.getHoraInicio();
            LocalTime horaNormalizada = horaInicio != null ? horaInicio.withSecond(0).withNano(0) : null;

            if (bloque.getDia() != null && horaNormalizada != null) {
                CeldaHorario celda = getCelda(bloque.getDia(), horaNormalizada);
                if (celda != null) {
                    celda.add(new BloquePanel(bloque), BorderLayout.CENTER);
                    continue;
                }
            }

            agregarBloqueSinAsignar(bloque);
        }

        panelSinAsignar.actualizarEstadoVacio();
        revalidate();
        repaint();
    }

    private void limpiarCeldasHorario() {
        for (Component comp : gridPanel.getComponents()) {
            if (comp instanceof CeldaHorario) {
                ((CeldaHorario) comp).removeAll();
            }
        }
    }

    private void agregarBloqueSinAsignar(BloqueHorario bloque) {
        BloquePanel panel = new BloquePanel(bloque);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelSinAsignar.addBloquePanel(panel);
    }

    private JLabel crearCeldaCabecera(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(new Color(220, 220, 220));
        return label;
    }

    private BloquePanel findBloquePanelById(String id) {
        for (Component comp : gridPanel.getComponents()) {
            if (comp instanceof CeldaHorario) {
                CeldaHorario celda = (CeldaHorario) comp;
                if (celda.getComponentCount() > 0 && celda.getComponent(0) instanceof BloquePanel) {
                    BloquePanel panel = (BloquePanel) celda.getComponent(0);
                    if (panel.getBloque().getId().equals(id)) return panel;
                }
            }
        }
        for (Component comp : panelSinAsignar.getComponents()) {
            if (comp instanceof BloquePanel) {
                BloquePanel panel = (BloquePanel) comp;
                if (panel.getBloque().getId().equals(id)) return panel;
            }
        }
        return null;
    }

    /**
     * Celda individual de la cuadr��cula que acepta Drops.
     */
    private class CeldaHorario extends JPanel implements DropTargetListener {
        
        String dia;
        LocalTime hora;
        PanelHorario panelHorarioPadre;

        public CeldaHorario(String dia, LocalTime hora, PanelHorario panelHorarioPadre) {
            this.dia = dia;
            this.hora = hora;
            this.panelHorarioPadre = panelHorarioPadre;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            new DropTarget(this, DnDConstants.ACTION_MOVE, this, true);
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
                        if (oldParent instanceof PanelSinAsignar) {
                            ((PanelSinAsignar) oldParent).actualizarEstadoVacio();
                        }
                    }
                    
                    this.removeAll(); // Solo un bloque por celda
                    this.add(bloquePanel, BorderLayout.CENTER);
                    
                    bloqueTransferido.setDia(this.dia);
                    
                    LocalTime nuevaHoraInicio = this.hora;
                    Duration duracion = bloqueTransferido.getDuracion();
                    bloqueTransferido.setHoraInicio(nuevaHoraInicio);
                    bloqueTransferido.setHoraFin(nuevaHoraInicio.plus(duracion));
                    
                    this.revalidate();
                    this.repaint();
                    dtde.dropComplete(true);
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dtde.rejectDrop();
            } finally {
                setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        }

        @Override public void dragEnter(DropTargetDragEvent dtde) {
            setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        }
        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dragExit(DropTargetEvent dte) {
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
    }
    
    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }

    private class PanelSinAsignar extends JPanel implements DropTargetListener {
        private final JLabel lblEmpty;

        public PanelSinAsignar() {
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
            if (lblEmpty.getParent() == this) {
                remove(lblEmpty);
            }
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
            if (!hayBloques && lblEmpty.getParent() != this) {
                add(lblEmpty);
            } else if (hayBloques && lblEmpty.getParent() == this) {
                remove(lblEmpty);
            }
            lblEmpty.setVisible(!hayBloques);
            revalidate();
            repaint();
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

        @Override public void dragEnter(DropTargetDragEvent dtde) {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLUE, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        }
        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dragExit(DropTargetEvent dte) {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
    }
}
