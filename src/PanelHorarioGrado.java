package src;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Panel que muestra una vista consolidada del horario para todos los grupos de un mismo grado.
 * Genera una cuadrícula con 5 columnas (días) por cada grupo.
 */
public class PanelHorarioGrado extends JPanel {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime[] HORAS_DIA = PlantillaHoraria.BLOQUES_ESTANDAR.toArray(new LocalTime[0]);
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    private final Map<String, CeldaHorarioGrado> celdas = new java.util.HashMap<>();
    private final List<GrupoEstudiantes> grupos;
    private List<BloqueHorario> bloques;
    private final PanelSinAsignarGrado panelSinAsignar;

    public PanelHorarioGrado(List<GrupoEstudiantes> grupos, List<BloqueHorario> bloques) {
        // Ordenar grupos por nombre para una visualización consistente
        this.grupos = grupos.stream()
                .sorted(java.util.Comparator.comparing(GrupoEstudiantes::getNombre))
                .collect(Collectors.toList());
        this.bloques = bloques;
        this.panelSinAsignar = new PanelSinAsignarGrado();

        setLayout(new BorderLayout(10, 10));

        // Panel principal para la cuadrícula
        int numColumnas = 1 + (DIAS_SEMANA.length * this.grupos.size());
        JPanel gridPanel = new JPanel(new GridLayout(0, numColumnas));
        gridPanel.setBackground(Color.WHITE);

        // --- Cabeceras ---
        gridPanel.add(new JLabel("")); // Esquina superior izquierda

        // Fila 1: Cabeceras de Grupos (cada una abarca 5 días)
        for (GrupoEstudiantes grupo : this.grupos) {
            gridPanel.add(crearCabeceraGrupoPrincipal(grupo.toString()));
            // Relleno para simular colspan
            for (int i = 0; i < DIAS_SEMANA.length-1; i++) {
                gridPanel.add(new JLabel(""));
            }
        }

        // Fila 2: Sub-cabeceras de Días
        gridPanel.add(new JLabel("")); // Columna de horas
        for (GrupoEstudiantes grupo : this.grupos) {
            for(String dia : DIAS_SEMANA) {
                gridPanel.add(crearCabeceraDia(dia));
            }
        }

        // --- Contenido de la cuadrícula ---
        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCabeceraHora(formatearHora(hora)));

            for (GrupoEstudiantes grupo : this.grupos) {
                for (String dia : DIAS_SEMANA) {
                    CeldaHorarioGrado celda = new CeldaHorarioGrado(dia, hora, grupo.getId());
                    celdas.put(celda.getKey(), celda);
                    gridPanel.add(celda);
                }
            }
        }

        // Panel de bloques sin asignar
        cargarBloques(bloques);

        JScrollPane scrollSinAsignar = new JScrollPane(panelSinAsignar);
        scrollSinAsignar.setPreferredSize(new Dimension(240, 0));
        scrollSinAsignar.setBorder(BorderFactory.createEmptyBorder());

        JPanel contenedorSinAsignar = new JPanel(new BorderLayout(5, 5));
        contenedorSinAsignar.setBorder(BorderFactory.createTitledBorder("Bloques sin asignar del grado"));
        contenedorSinAsignar.add(scrollSinAsignar, BorderLayout.CENTER);

        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        add(contenedorSinAsignar, BorderLayout.EAST);
    }

    public void cargarBloques(List<BloqueHorario> bloques) {
        this.bloques = bloques;
        // Limpiar celdas y panel sin asignar
        celdas.values().forEach(CeldaHorarioGrado::reset);
        panelSinAsignar.resetContenido();

        // Colocar bloques
        for (BloqueHorario bloque : bloques) {
            if (bloque.getDia() != null && bloque.getHoraInicio() != null) {
                String key = CeldaHorarioGrado.generarKey(bloque.getDia(), bloque.getHoraInicio(), bloque.getGrupoId());
                CeldaHorarioGrado celda = celdas.get(key);
                if (celda != null) {
                    celda.colocarBloque(new BloquePanel(bloque, false));
                    continue;
                }
            }
            // Si no se pudo colocar, va a sin asignar
            panelSinAsignar.addBloquePanel(new BloquePanel(bloque, false));
        }
        panelSinAsignar.actualizarEstadoVacio();
        revalidate();
        repaint();
    }

    public List<BloqueHorario> obtenerTodosLosBloques() {
        List<BloqueHorario> todosBloques = new ArrayList<>();

        for (CeldaHorarioGrado celda : celdas.values()) {
            BloquePanel panel = celda.obtenerBloquePanel();
            if (panel != null) {
                todosBloques.add(panel.getBloque());
            }
        }

        for (Component comp : panelSinAsignar.getComponents()) {
            if (comp instanceof BloquePanel) {
                todosBloques.add(((BloquePanel) comp).getBloque());
            }
        }
        return todosBloques;
    }

    private JLabel crearCabeceraGrupoPrincipal(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setOpaque(true);
        label.setBackground(new Color(46, 78, 126));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(30, 50, 100)));
        return label;
    }

    private JLabel crearCabeceraDia(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(78, 115, 223));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(60, 80, 140)));
        return label;
    }

    private JLabel crearCabeceraHora(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(223, 230, 251));
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 210, 240)));
        return label;
    }

    private String formatearHora(LocalTime hora) {
        return hora.format(HORA_FORMATTER);
    }

    /**
     * Celda de solo lectura utilizada en la vista general del grado.
     */
    public class CeldaHorarioGrado extends JPanel {
        private final String dia;
        private final LocalTime hora;
        private final String grupoId;

        CeldaHorarioGrado(String dia, LocalTime hora, String grupoId) {
            this.dia = dia;
            this.hora = hora;
            this.grupoId = grupoId;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(230, 230, 230)));
        }

        public String getDia() { return dia; }
        public LocalTime getHora() { return hora; }
        public String getGrupoId() { return grupoId; }

        public String getKey() {
            return generarKey(dia, hora, grupoId);
        }

        public static String generarKey(String dia, LocalTime hora, String grupoId) {
            return dia + "-" + hora.toString() + "-" + grupoId;
        }

        public void colocarBloque(BloquePanel panel) {
            removeAll();
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        public BloquePanel obtenerBloquePanel() {
            if (getComponentCount() == 0) {
                return null;
            }
            Component comp = getComponent(0);
            return comp instanceof BloquePanel ? (BloquePanel) comp : null;
        }

        public void reset() {
            removeAll();
            revalidate();
            repaint();
        }
    }

    private class PanelSinAsignarGrado extends JPanel {
        private final JLabel lblEmpty;

        PanelSinAsignarGrado() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(248, 248, 248));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            lblEmpty = new JLabel("Bloques pendientes de asignar", SwingConstants.CENTER);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblEmpty);
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
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
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
    }
}
