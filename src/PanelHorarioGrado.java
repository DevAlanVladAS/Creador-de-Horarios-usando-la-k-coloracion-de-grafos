package src;

import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.Transferable;
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
    private final PanelHorario.PanelSinAsignar panelSinAsignar;

    public PanelHorarioGrado(List<GrupoEstudiantes> grupos, List<BloqueHorario> bloques) {
        // Ordenar grupos por nombre para una visualización consistente
        this.grupos = grupos.stream()
                .sorted(java.util.Comparator.comparing(GrupoEstudiantes::getNombre))
                .collect(Collectors.toList());
        this.bloques = bloques;

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
        DropTargetListener dropListener = crearDropTargetListener();
        for (LocalTime hora : HORAS_DIA) {
            gridPanel.add(crearCabeceraHora(formatearHora(hora)));

            for (GrupoEstudiantes grupo : this.grupos) {
                for (String dia : DIAS_SEMANA) {
                    CeldaHorarioGrado celda = new CeldaHorarioGrado(dia, hora, grupo.getId());
                    new DropTarget(celda, DnDConstants.ACTION_MOVE, dropListener, true);
                    celdas.put(celda.getKey(), celda);
                    gridPanel.add(celda);
                }
            }
        }

        // Panel de bloques sin asignar
        panelSinAsignar = PanelHorario.crearPanelSinAsignar();
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
                    celda.colocarBloque(new BloquePanel(bloque));
                    continue;
                }
            }
            // Si no se pudo colocar, va a sin asignar
            panelSinAsignar.addBloquePanel(new BloquePanel(bloque));
        }
        panelSinAsignar.actualizarEstadoVacio();
        revalidate();
        repaint();
    }

    public List<BloqueHorario> obtenerTodosLosBloques() {
    List<BloqueHorario> todosBloques = new ArrayList<>();
    
    // Bloques en las celdas
    for (CeldaHorarioGrado celda : celdas.values()) {
        BloquePanel panel = celda.obtenerBloquePanel();
        if (panel != null) {
            todosBloques.add(panel.getBloque());
        }
    }
    
    // Bloques sin asignar
    for (Component comp : panelSinAsignar.getComponents()) {
        if (comp instanceof BloquePanel) {
            todosBloques.add(((BloquePanel) comp).getBloque());
        }
    }
    
    return todosBloques;
    }

    private java.util.Optional<BloqueHorario> findBloqueParaCelda(String grupoId, String dia, LocalTime hora) {
        return bloques.stream()
                .filter(b -> b.getDia() != null && b.getHoraInicio() != null &&
                        b.getGrupoId().equals(grupoId) &&
                        b.getDia().equalsIgnoreCase(dia) &&
                        b.getHoraInicio().equals(hora))
                .findFirst();
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

    private DropTargetListener crearDropTargetListener() {
        return new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                Component comp = dtde.getDropTargetContext().getComponent();
                if (comp instanceof CeldaHorarioGrado) {
                    ((CeldaHorarioGrado) comp).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(78, 115, 223)));
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {
                Component comp = dte.getDropTargetContext().getComponent();
                if (comp instanceof CeldaHorarioGrado) {
                    ((CeldaHorarioGrado) comp).actualizarBorde(false, false);
                }
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                Component comp = dtde.getDropTargetContext().getComponent();
                if (!(comp instanceof CeldaHorarioGrado)) {
                    dtde.rejectDrop();
                    return;
                }
                CeldaHorarioGrado celda = (CeldaHorarioGrado) comp;
                try {
                    Transferable tr = dtde.getTransferable();
                    if (!tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                        dtde.rejectDrop();
                        return;
                    }

                    BloqueHorario bloqueTransferido = (BloqueHorario) tr.getTransferData(BloquePanel.DATA_FLAVOR);

                    // Validar que el bloque pertenece a este grupo
                    if (!bloqueTransferido.getGrupoId().equals(celda.getGrupoId())) {
                        dtde.rejectDrop();
                        return;
                    }

                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    
                    bloqueTransferido.setDia(celda.getDia());
                    bloqueTransferido.setHoraInicio(celda.getHora());
                    bloqueTransferido.setHoraFin(celda.getHora().plus(PlantillaHoraria.DURACION_BLOQUE));

                    dtde.getDropTargetContext().dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.rejectDrop();
                } finally {
                    celda.actualizarBorde(false, false);
                }
            }
        };
    }

    /**
     * Celda especializada para el horario de grado, que también conoce el ID del grupo.
     */
    public class CeldaHorarioGrado extends PanelHorario.CeldaHorario {
        private final String grupoId;

        CeldaHorarioGrado(String dia, LocalTime hora, String grupoId) {
            super(dia, hora);
            this.grupoId = grupoId;
        }

        public String getGrupoId() {
            return grupoId;
        }

        public String getKey() {
            return generarKey(getDia(), getHora(), grupoId);
        }

        public static String generarKey(String dia, LocalTime hora, String grupoId) {
            return dia + "-" + hora.toString() + "-" + grupoId;
        }

        @Override
        public void colocarBloque(BloquePanel panel) {
            // Validar que el bloque que se intenta colocar pertenece a este grupo
            if (!panel.getBloque().getGrupoId().equals(this.grupoId)) {
                 JOptionPane.showMessageDialog(this,
                         "Este bloque pertenece a otro grupo y no puede ser colocado aquí.",
                         "Error de asignación",
                         JOptionPane.ERROR_MESSAGE);
                 // Idealmente, el bloque debería volver a su lugar original.
                 return;
            }
            super.colocarBloque(panel);
        }
    }
}