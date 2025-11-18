package src;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa visualmente un BloqueHorario y puede arrastrarse entre celdas.
 */
public class BloquePanel extends JPanel implements Transferable, DragGestureListener {

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(BloqueHorario.class, "BloqueHorario");

    private static final Color[] PALETA = new Color[]{
            new Color(88, 129, 234),
            new Color(0, 172, 193),
            new Color(255, 140, 66),
            new Color(87, 75, 168),
            new Color(0, 150, 136),
            new Color(156, 39, 176),
            new Color(233, 152, 0),
            new Color(46, 125, 50)
    };
    private static final Map<String, Color> COLORES_POR_PROFESOR = new HashMap<>();

    private final BloqueHorario bloque;
    private final Color baseColor;
    private boolean mergeTop;
    private boolean mergeBottom;

    public BloquePanel(BloqueHorario bloque) {
        this.bloque = bloque;
        this.baseColor = asignarColor(bloque.getProfesorId());
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        Salon salon = catalogo.obtenerSalonPorId(bloque.getSalonId());

        JLabel materiaLabel = new JLabel(bloque.getMateria() != null ? bloque.getMateria() : "Materia");
        materiaLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        materiaLabel.setForeground(Color.WHITE);

        JLabel profesorLabel = new JLabel(
                profesor != null ? profesor.getNombre() : "Profesor sin asignar");
        profesorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        profesorLabel.setForeground(Color.WHITE);

        JLabel salonLabel = new JLabel(
                salon != null ? salon.getNombre() : "");
        salonLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        salonLabel.setForeground(Color.WHITE);

        add(materiaLabel);
        add(Box.createVerticalStrut(2));
        add(profesorLabel);
        if (!salonLabel.getText().isEmpty()) {
            add(salonLabel);
        }

        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
    }

    private Color asignarColor(String profesorId) {
        if (profesorId == null) {
            return new Color(120, 120, 120);
        }
        return COLORES_POR_PROFESOR.computeIfAbsent(
                profesorId,
                id -> PALETA[COLORES_POR_PROFESOR.size() % PALETA.length]
        );
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
        dge.startDrag(DragSource.DefaultMoveDrop, this);
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
