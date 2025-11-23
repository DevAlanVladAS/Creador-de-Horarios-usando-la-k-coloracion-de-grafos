package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.HashMap;
import java.util.Map;
import java.awt.Cursor;

public class BloquePanel extends JPanel implements Transferable, DragGestureListener, DragSourceListener {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(BloqueHorario.class, "BloqueHorario");

    private static final Color[] PALETA = new Color[]{
            new Color(244, 143, 177),
            new Color(255, 167, 38),
            new Color(102, 187, 106),
            new Color(255, 202, 40),
            new Color(171, 71, 188),
            new Color(255, 112, 67),
            new Color(0, 150, 136),
            new Color(124, 179, 66)
    };
    private static final Color COLOR_CABECERA_DIA = new Color(78, 115, 223);
    private static final Map<String, Color> COLORES_POR_PROFESOR = new HashMap<>();

    private final BloqueHorario bloque;
    private final Color baseColor;
    private final boolean arrastrable;
    private boolean mergeTop;
    private boolean mergeBottom;

    public BloquePanel(BloqueHorario bloque) {
        this(bloque, true);
    }

    public BloquePanel(BloqueHorario bloque, boolean habilitarArrastre) {
        this.bloque = bloque;
        this.arrastrable = habilitarArrastre;
        this.baseColor = asignarColor(bloque.getProfesorId());
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        Salon salon = catalogo.obtenerSalonPorId(bloque.getSalonId());
        GrupoEstudiantes grupo = catalogo.obtenerGrupoPorId(bloque.getGrupoId());

        JLabel materiaLabel = new JLabel(bloque.getMateria() != null ? bloque.getMateria() : "Materia");
        materiaLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        materiaLabel.setForeground(Color.WHITE);

        JLabel profesorLabel = new JLabel(
                profesor != null ? profesor.getNombre() : "Profesor sin asignar");
        profesorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        profesorLabel.setForeground(Color.WHITE);

        JLabel grupoLabel = new JLabel(
                grupo != null ? "Grupo " + grupo.getNombre() : "");
        grupoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        grupoLabel.setForeground(Color.WHITE);

        JLabel salonLabel = new JLabel(
                salon != null ? salon.getNombre() : "");
        salonLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        salonLabel.setForeground(Color.WHITE);

        add(materiaLabel);
        add(Box.createVerticalStrut(2));
        add(profesorLabel);
        if (!grupoLabel.getText().isEmpty()) {
            add(grupoLabel);
        }
        if (!salonLabel.getText().isEmpty()) {
            add(salonLabel);
        }

        if (arrastrable) {
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        }
    }

    private Color asignarColor(String profesorId) {
        if (profesorId == null) {
            return new Color(120, 120, 120);
        }
        return COLORES_POR_PROFESOR.computeIfAbsent(profesorId, id -> seleccionarColorDisponible());
    }

    private Color seleccionarColorDisponible() {
        int offset = COLORES_POR_PROFESOR.size();
        for (int i = 0; i < PALETA.length; i++) {
            Color candidato = PALETA[(offset + i) % PALETA.length];
            if (!esColorCabecera(candidato)) {
                return candidato;
            }
        }
        return COLOR_CABECERA_DIA.brighter();
    }

    private boolean esColorCabecera(Color color) {
        return color.getRed() == COLOR_CABECERA_DIA.getRed()
                && color.getGreen() == COLOR_CABECERA_DIA.getGreen()
                && color.getBlue() == COLOR_CABECERA_DIA.getBlue();
    }

    public BloqueHorario getBloque() {
        return bloque;
    }

    public void setMergeState(boolean mergeTop, boolean mergeBottom) {
        this.mergeTop = mergeTop;
        this.mergeBottom = mergeBottom;
        repaint();
    }

    public void resetMergeState() {
        setMergeState(false, false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        g2.setColor(baseColor);
        g2.fillRoundRect(0, 0, width, height, 14, 14);
        if (mergeTop) {
            g2.fillRect(0, 0, width, 8);
        }
        if (mergeBottom) {
            g2.fillRect(0, height - 8, width, 8);
        }
        g2.setColor(baseColor.darker());
        g2.drawRoundRect(0, 0, width - 1, height - 1, 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        // Iniciar el arrastre y registrar ESTE panel como el listener de la fuente.
        dge.startDrag(DragSource.DefaultMoveDrop, this, this);
    }

    // --- Métodos de DragSourceListener ---

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {}

    @Override
    public void dragOver(DragSourceDragEvent dsde) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}

    @Override
    public void dragExit(DragSourceEvent dse) {}

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // Este método se llama cuando el drop se completa.
        // Si el drop fue exitoso, eliminamos el panel original de su contenedor.
        if (dsde.getDropSuccess()) {
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
            }
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        if (isDataFlavorSupported(flavor)) {
            return bloque;
        }
        return null;
    }
}
