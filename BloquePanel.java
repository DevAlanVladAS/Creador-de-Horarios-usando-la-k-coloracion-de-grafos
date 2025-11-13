import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

/**
 * Un panel visual que representa un BloqueHorario y que
 * puede ser arrastrado (Drag-and-Drop).
 */
public class BloquePanel extends JPanel implements Transferable, DragGestureListener {

    // DataFlavor personalizado para transferir este panel
    public static final DataFlavor DATA_FLAVOR = new DataFlavor(BloquePanel.class, "BloquePanel");

    private BloqueHorario bloque;

    public BloquePanel(BloqueHorario bloque) {
        this.bloque = bloque;
        
        // Configuración visual
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(200, 220, 255));
        setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Asumiendo que CatalogoRecursos está disponible
        CatalogoRecursos catalogo = CatalogoRecursos.getInstance();
        Profesor p = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        
        String profNombre = (p != null) ? p.getNombre() : "ID: " + bloque.getProfesorId();
        
        add(new JLabel(bloque.getMateria()));
        add(new JLabel(profNombre));
        
        // Configurar la fuente del Drag and Drop
        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
    }

    public BloqueHorario getBloque() {
        return bloque;
    }

    // --- Métodos de DragGestureListener ---

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        // Inicia el arrastre
        dge.startDrag(DragSource.DefaultMoveDrop, this);
    }

    // --- Métodos de Transferable ---

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
        return this; // Transfiere la instancia de este panel
    }
}