package src;

import javax.swing.*;
import java.awt.*;

/**
 * Formulario Swing para capturar nombre de escuela y cantidad de grupos por grado,
 * con acceso a gestion de recursos.
 */
public class PanelConfiguracionProyecto extends JPanel {

    private final JTextField txtNombreEscuela = new JTextField(25);
    private final JSpinner[] spinnersGrado = new JSpinner[3];
    private final JButton btnGestionarRecursos = new JButton("Gestionar recursos...");

    /**
     * Construye el panel y arma el formulario de configuracion inicial.
     */
    public PanelConfiguracionProyecto() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Configura los datos iniciales de tu proyecto");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(new JLabel("Nombre de la escuela:"), gbc);

        gbc.gridx = 1;
        txtNombreEscuela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formulario.add(txtNombreEscuela, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formulario.add(new JLabel("Grupos por grado:"), gbc);

        JPanel panelGrados = new JPanel(new GridLayout(3, 2, 10, 6));
        for (int grado = 1; grado <= 3; grado++) {
            panelGrados.add(new JLabel(grado + "º grado"));
            spinnersGrado[grado - 1] = crearSpinner();
            panelGrados.add(spinnersGrado[grado - 1]);
        }

        gbc.gridy = 2;
        formulario.add(panelGrados, gbc);

        gbc.gridy = 3;
        btnGestionarRecursos.setFocusPainted(false);
        btnGestionarRecursos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formulario.add(btnGestionarRecursos, gbc);

        add(formulario, BorderLayout.CENTER);
    }

    private JSpinner crearSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(1, 0, 15, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        return spinner;
    }

    /**
     * Carga en el formulario una configuracion existente.
     */
    public void setConfiguracion(ConfiguracionProyecto configuracion) {
        if (configuracion == null) {
            return;
        }
        txtNombreEscuela.setText(configuracion.getNombreEscuela());
        for (int grado = 1; grado <= 3; grado++) {
            spinnersGrado[grado - 1].setValue(configuracion.getCantidadGrupos(grado));
        }
    }

    public ConfiguracionProyecto construirConfiguracion() {
        ConfiguracionProyecto configuracion = new ConfiguracionProyecto();
        configuracion.setNombreEscuela(txtNombreEscuela.getText().trim());
        for (int grado = 1; grado <= 3; grado++) {
            configuracion.setCantidadGrupos(grado, (Integer) spinnersGrado[grado - 1].getValue());
        }
        return configuracion;
    }

    /**
     * Registra listener para el boton "Gestionar recursos...".
     */
    public void addGestionarRecursosListener(java.awt.event.ActionListener listener) {
        btnGestionarRecursos.addActionListener(listener);
    }
}
