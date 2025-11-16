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
 * Panel que muestra la cuadrícula del horario de un Grupo.
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
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    public PanelHorario() {
        this.grupoId = grupoId;
        setLayout(new BorderLayout(10, 10));
        
        // Layout de Cuadrícula: 10 filas (9 horas + 1 cabecera) x 6 cols (5 dias + 1 cabecera)
        JPanel gridPanel = new JPanel(new GridLayout(HORAS_DIA.length + 1, DIAS_SEMANA.length + 1));
        
        // 1. Esquina vacía
        gridPanel.add(new JLabel("")); 
        
        // 2. Cabecera Días
        for (String dia : DIAS_SEMANA) {
            gridPanel.add(crearCeldaCabecera(dia));
        }
        
        // 3. Cabecera Horas y Celdas de Horario
        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCeldaCabecera(formatearHora(hora))); // Cabecera de Hora
            for (String dia : DIAS_SEMANA) {
                // Celda que acepta el Drop
                gridPanel.add(new CeldaHorario(dia, hora, this)); 
            }
        }
        
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
    }
    
    /**
     * Busca una celda específica en la cuadrícula por día y hora.
     * @param dia El día de la semana (e.g., "Lunes").
     * @param hora La hora en formato "HH:mm" (e.g., "07:00").
     * @return La CeldaHorario correspondiente, o null si no se encuentra.
     */
    private CeldaHorario getCelda(String dia, LocalTime hora) {
        if (dia == null || hora == null) {
            return null;
        }
        for (Component comp : ((JPanel) ((JScrollPane) getComponent(0)).getViewport().getView()).getComponents()) {
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
     * Carga una lista de bloques en el panel del horario, colocándolos en sus celdas correspondientes.
     * Este método reemplaza la simulación y permite cargar datos reales.
     * @param bloques La lista de BloqueHorario a mostrar.
     */
    public void cargarBloques(List<BloqueHorario> bloques) {
        for (BloqueHorario bloque : bloques) {
            LocalTime horaInicio = bloque.getHoraInicio();
            if (horaInicio == null) {
                continue;
            }
            horaInicio = horaInicio.withSecond(0).withNano(0);
            CeldaHorario celda = getCelda(bloque.getDia(), horaInicio);
            if (celda != null) {
                celda.add(new BloquePanel(bloque), BorderLayout.CENTER);
            }
        }
        revalidate();
        repaint();
    }

    private JLabel crearCeldaCabecera(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(new Color(220, 220, 220));
        return label;
    }

    /**
     * Celda individual de la cuadrícula que acepta Drops.
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

        // --- Métodos de DropTargetListener ---
        
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable tr = dtde.getTransferable();
                if (tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    
                    BloqueHorario bloqueTransferido = (BloqueHorario) tr.getTransferData(BloquePanel.DATA_FLAVOR);
                    
                    // Buscar el componente BloquePanel correspondiente al BloqueHorario
                    BloquePanel bloquePanel = findBloquePanelById(bloqueTransferido.getId());

                    if (bloquePanel == null) {
                        // No se encontró el panel, algo salió mal.
                        // Esto podría pasar si el panel no está visible o ya fue removido.
                        System.err.println("Error: No se encontró el BloquePanel para el ID: " + bloqueTransferido.getId());
                        dtde.rejectDrop();
                        return;
                    }

                    // --- LÓGICA DE MOVIMIENTO ---
                    // 1. Quitar del panel anterior
                    Container oldParent = bloquePanel.getParent();
                    if (oldParent != null) {
                        oldParent.remove(bloquePanel);
                        oldParent.revalidate();
                        oldParent.repaint();
                    }
                    
                    // 2. Añadir al nuevo panel (esta celda)
                    this.removeAll(); // Solo un bloque por celda
                    this.add(bloquePanel, BorderLayout.CENTER);
                    
                    // 3. Actualizar el Modelo (BloqueHorario)
                    // Actualiza el día
                    bloqueTransferido.setDia(this.dia);
                    
                    // Actualiza la hora, conservando la duración del bloque
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
            }
        }

        /**
         * Busca recursivamente en el PanelHorario un BloquePanel que coincida con el ID del BloqueHorario.
         */
        private BloquePanel findBloquePanelById(String id) {
            for (Component comp : ((JPanel) ((JScrollPane) panelHorarioPadre.getComponent(0)).getViewport().getView()).getComponents()) {
                if (comp instanceof CeldaHorario) {
                    CeldaHorario celda = (CeldaHorario) comp;
                    if (celda.getComponentCount() > 0 && celda.getComponent(0) instanceof BloquePanel) {
                        BloquePanel panel = (BloquePanel) celda.getComponent(0);
                        if (panel.getBloque().getId().equals(id)) return panel;
                    }
                }
            }
            return null; // No encontrado
        }
        
        // (Otros métodos de DropTargetListener: dragEnter, dragOver, dragExit, dropActionChanged)
        @Override public void dragEnter(DropTargetDragEvent dtde) {
            setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); // Resaltar al entrar
        }
        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dragExit(DropTargetEvent dte) {
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Quitar resaltado
        }
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
    }
    
    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }
}
