package src;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Panel encargado de exportar los horarios a formato PNG o guardar progreso.
 */
public class PanelExportacion extends JPanel {

    private ControladorPersistencia controladorPersistencia = new ControladorPersistencia();
    private Supplier<HorarioSemana> proveedorHorario;
    private Supplier<Component> proveedorVista;

    public PanelExportacion() {
        setLayout(new GridLayout(2, 1, 10, 10));
        JButton btnExportar = new JButton("Exportar a PNG");
        JButton btnGuardar = new JButton("Guardar Progreso (JSON)");

        btnExportar.addActionListener(e -> exportarPNG());
        btnGuardar.addActionListener(e -> guardarProgresoJSON());

        add(btnExportar);
        add(btnGuardar);
    }

    public void setControladorPersistencia(ControladorPersistencia controladorPersistencia) {
        if (controladorPersistencia != null) {
            this.controladorPersistencia = controladorPersistencia;
        }
    }

    public void setProveedorHorario(Supplier<HorarioSemana> proveedorHorario) {
        this.proveedorHorario = proveedorHorario;
    }

    public void setProveedorVista(Supplier<Component> proveedorVista) {
        this.proveedorVista = proveedorVista;
    }

    public void exportarPNG() {
        if (proveedorVista == null) {
            mostrarMensaje("No hay una vista configurada para exportar.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Component componente = proveedorVista.get();
        if (componente == null) {
            mostrarMensaje("No se encontr\u00f3 el componente a exportar.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = crearChooser("Imagen PNG", "png");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = asegurarExtension(chooser.getSelectedFile(), "png");
        Dimension size = componente.getSize();
        if (size.width <= 0 || size.height <= 0) {
            size = componente.getPreferredSize();
            componente.setSize(size);
            componente.doLayout();
        }
        BufferedImage imagen = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        componente.printAll(g2);
        g2.dispose();
        try {
            ImageIO.write(imagen, "png", archivo);
            mostrarMensaje("Exportaci\u00f3n completada: " + archivo.getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarMensaje("Error al exportar PNG: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void guardarProgresoJSON() {
        if (proveedorHorario == null) {
            mostrarMensaje("No hay un horario configurado para guardar.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HorarioSemana horario = proveedorHorario.get();
        if (horario == null) {
            mostrarMensaje("El horario actual es nulo.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = crearChooser("Archivo JSON", "json");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = asegurarExtension(chooser.getSelectedFile(), "json");
        try {
            controladorPersistencia.guardar(horario, archivo.toPath());
            mostrarMensaje("Horario guardado en " + archivo.getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarMensaje("No se pudo guardar el horario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFileChooser crearChooser(String descripcion, String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(descripcion);
        chooser.setFileFilter(new FileNameExtensionFilter(descripcion, extension));
        return chooser;
    }

    private File asegurarExtension(File archivo, String extension) {
        String nombre = archivo.getName().toLowerCase();
        if (!nombre.endsWith("." + extension)) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extension);
        }
        return archivo;
    }

    private void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, "Exportaci\u00f3n", tipo);
    }
}
