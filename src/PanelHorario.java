package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.time.LocalTime;
import java.util.List;

/**
 * Panel que muestra la cuadrícula del horario de un Grupo.
 * Implementa Drag-and-Drop para los BloquePanel.
 */
public class PanelHorario extends JPanel {

    private String grupoId;
    private final String[] HORAS_DIA = {"7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    public PanelHorario(String grupoId) {
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
        for (String hora : HORAS_DIA) {
            gridPanel.add(crearCeldaCabecera(hora)); // Cabecera de Hora
            for (String dia : DIAS_SEMANA) {
                // Celda que acepta el Drop
                gridPanel.add(new CeldaHorario(dia, hora)); 
            }
        }
        
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        
        // Cargar los bloques existentes (simulación)
        simularCargaHorario();
    }
    
    private void simularCargaHorario() {
        // Simulación: Añadir un bloque de ejemplo al catálogo
        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        if (catalogo.getTodosLosProfesores().isEmpty()) {
            catalogo.addProfesor(new Profesor("Prof. Simulado", "Algebra", List.of("Lunes"), List.of("7:00")));
        }
        Profesor p = catalogo.getTodosLosProfesores().get(0);
        
        BloqueHorario bloqueSimulado = new BloqueHorario(
            LocalTime.of(7, 0), LocalTime.of(8, 0),
            p.getMateriaAsignada(), p.getId(), "A1", grupoId, false
        );
        bloqueSimulado.setDia("Lunes"); // Asignar día

        // Buscar la celda "Lunes" a las "7:00" y añadir el panel
        for (Component comp : ((JPanel) ((JScrollPane) getComponent(0)).getViewport().getView()).getComponents()) {
            if (comp instanceof CeldaHorario) {
                CeldaHorario celda = (CeldaHorario) comp;
                if (celda.dia.equals("Lunes") && celda.hora.equals("7:00")) {
                    celda.add(new BloquePanel(bloqueSimulado));
                    break;
                }
            }
        }
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
        String hora;

        public CeldaHorario(String dia, String hora) {
            this.dia = dia;
            this.hora = hora;
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
                    
                    BloquePanel bloquePanel = (BloquePanel) tr.getTransferData(BloquePanel.DATA_FLAVOR);
                    
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
                    bloquePanel.getBloque().setDia(this.dia);
                    // (Aquí también actualizarías la hora, pero BloqueHorario usa LocalTime)
                    
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
}