package src;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.Transferable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel que muestra una vista consolidada del horario para todos los grupos de un grado
 * (grid dias x grupos) y sincroniza cambios con GestorHorarios via observer.
 */
public class PanelHorarioGrado extends JPanel implements GestorHorarios.HorarioChangeListener {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime[] HORAS_DIA = PlantillaHoraria.BLOQUES_ESTANDAR.toArray(new LocalTime[0]);
    private final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"};

    private final Map<String, CeldaHorarioGrado> celdas = new java.util.HashMap<>();
    private final List<GrupoEstudiantes> grupos;
    private final PanelHorario.PanelSinAsignar panelSinAsignar;
    private final List<String> grupoIds;
    
    private final GestorHorarios gestor;
    private boolean refrescando = false;

    /**
     * Construye el panel recibiendo los grupos de un grado y arma la cuadricula.
     */
    public PanelHorarioGrado(List<GrupoEstudiantes> grupos) {
        this.grupos = grupos.stream()
                .sorted(java.util.Comparator.comparing(GrupoEstudiantes::getNombre))
                .collect(Collectors.toList());
        
        this.grupoIds = this.grupos.stream()
                .map(GrupoEstudiantes::getId)
                .collect(Collectors.toList());
        
        this.gestor = GestorHorarios.getInstance();
        
        setLayout(new BorderLayout(10, 10));

        int numColumnas = 1 + (DIAS_SEMANA.length * this.grupos.size());
        JPanel gridPanel = new JPanel(new GridLayout(0, numColumnas));
        gridPanel.setBackground(Color.WHITE);

        // Cabeceras
        gridPanel.add(new JLabel(""));

        for (GrupoEstudiantes grupo : this.grupos) {
            gridPanel.add(crearCabeceraGrupoPrincipal(grupo.toString()));
            for (int i = 0; i < DIAS_SEMANA.length-1; i++) {
                gridPanel.add(new JLabel(""));
            }
        }

        gridPanel.add(new JLabel(""));
        for (GrupoEstudiantes grupo : this.grupos) {
            for(String dia : DIAS_SEMANA) {
                gridPanel.add(crearCabeceraDia(dia));
            }
        }

        // Contenido de la cuadrícula
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

        panelSinAsignar = PanelHorario.crearPanelSinAsignar();

        JScrollPane scrollSinAsignar = new JScrollPane(panelSinAsignar);
        scrollSinAsignar.setPreferredSize(new Dimension(240, 0));
        scrollSinAsignar.setBorder(BorderFactory.createEmptyBorder());

        JPanel contenedorSinAsignar = new JPanel(new BorderLayout(5, 5));
        contenedorSinAsignar.setBorder(BorderFactory.createTitledBorder("Bloques sin asignar del grado"));
        contenedorSinAsignar.add(scrollSinAsignar, BorderLayout.CENTER);

        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        add(contenedorSinAsignar, BorderLayout.EAST);
        
        gestor.addListener(this);
        
        refrescarVista();
    }

    /**
     * Grupos del grado (defensivo).
     */
    public List<GrupoEstudiantes> getGrupos() {
        return new ArrayList<>(grupos);
    }

    // ========== OBSERVER ==========

    /**
     * Callback de cambios en bloques; refresca si afecta a nuestros grupos.
     */
    @Override
    public void onBloquesChanged(String grupoIdAfectado, GestorHorarios.TipoCambio tipoCambio, 
                                BloqueHorario bloqueAfectado) {
        if (grupoIdAfectado == null || grupoIds.contains(grupoIdAfectado)) {
            SwingUtilities.invokeLater(this::refrescarVista);
        }
    }

    /**
     * Refresca la vista completa leyendo del gestor.
     */
    private void refrescarVista() {
        if (refrescando) return;
        
        try {
            refrescando = true;
            
            celdas.values().forEach(CeldaHorarioGrado::reset);
            panelSinAsignar.resetContenido();

            List<BloqueHorario> todosBloques = gestor.getBloquesGrado(grupoIds);

            for (BloqueHorario bloque : todosBloques) {
                LocalTime horaInicio = bloque.getHoraInicio();
                LocalTime horaNormalizada = horaInicio != null ? 
                    horaInicio.withSecond(0).withNano(0) : null;
                
                if (bloque.getDia() != null && horaNormalizada != null) {
                    String key = CeldaHorarioGrado.generarKey(
                        bloque.getDia(), horaNormalizada, bloque.getGrupoId());
                    CeldaHorarioGrado celda = celdas.get(key);
                    
                    if (celda != null) {
                        celda.colocarBloque(new BloquePanel(bloque));
                        continue;
                    }
                }
                
                panelSinAsignar.addBloquePanel(new BloquePanel(bloque));
            }
            
            panelSinAsignar.actualizarEstadoVacio();
            revalidate();
            repaint();
            
        } finally {
            refrescando = false;
        }
    }

    /**
     * Carga bloques (compatibilidad legacy) delegando al gestor.
     */
    public void cargarBloques(List<BloqueHorario> bloques) {
        Map<String, List<BloqueHorario>> bloquesPorGrupo = bloques.stream()
            .collect(Collectors.groupingBy(BloqueHorario::getGrupoId));
        
        for (String grupoId : grupoIds) {
            HorarioSemana semana = gestor.getHorarioSemana(grupoId);
            
            List<BloqueHorario> bloquesExistentes = new ArrayList<>(semana.getBloques());
            for (BloqueHorario bloqueExistente : bloquesExistentes) {
                semana.eliminarBloque(bloqueExistente.getId());
            }
            
            List<BloqueHorario> bloquesGrupo = bloquesPorGrupo.getOrDefault(grupoId, new ArrayList<>());
            for (BloqueHorario bloque : bloquesGrupo) {
                gestor.agregarBloque(bloque, grupoId);
                
                if (bloque.getDia() != null && bloque.getHoraInicio() != null) {
                    gestor.actualizarPosicionBloque(bloque, 
                        bloque.getDia(), 
                        bloque.getHoraInicio());
                }
            }
        }
        
        // El observer refrescara la vista
    }

    /**
     * Obtiene todos los bloques actuales del grado (para persistencia).
     */
    public List<BloqueHorario> obtenerTodosLosBloques() {
        return gestor.getBloquesGrado(grupoIds);
    }

    // ========== COMPONENTES GRAFICOS ==========

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
     * Limpieza al destruir el panel.
     */
    public void dispose() {
        gestor.removeListener(this);
    }

    // ========== DROP TARGET LISTENER ==========

    private DropTargetListener crearDropTargetListener() {
        return new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                Component comp = dtde.getDropTargetContext().getComponent();
                if (comp instanceof CeldaHorarioGrado) {
                    ((CeldaHorarioGrado) comp).setBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(78, 115, 223)));
                }
            }

            @Override public void dragOver(DropTargetDragEvent dtde) {}
            @Override public void dropActionChanged(DropTargetDragEvent dtde) {}

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
                boolean aceptado = false;
                
                try {
                    Transferable tr = dtde.getTransferable();
                    if (!tr.isDataFlavorSupported(BloquePanel.DATA_FLAVOR)) {
                        dtde.rejectDrop();
                        return;
                    }

                    BloqueHorario bloqueTransferido = (BloqueHorario) 
                        tr.getTransferData(BloquePanel.DATA_FLAVOR);

                    if (!bloqueTransferido.getGrupoId().equals(celda.getGrupoId())) {
                        dtde.rejectDrop();
                        JOptionPane.showMessageDialog(PanelHorarioGrado.this,
                            "Este bloque pertenece al grupo " + bloqueTransferido.getGrupoId() + 
                            " y no puede ser colocado en el grupo " + celda.getGrupoId(),
                            "Error de asignacion",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    aceptado = true;
                    
                    gestor.actualizarPosicionBloque(bloqueTransferido, celda.getDia(), celda.getHora());
                    
                    dtde.dropComplete(true);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!aceptado) {
                        dtde.rejectDrop();
                    } else {
                        dtde.dropComplete(false);
                    }
                } finally {
                    celda.actualizarBorde(false, false);
                }
            }
        };
    }

    // ========== CELDA ESPECIALIZADA ==========

    /**
     * Celda especializada para la vista de grado (incluye id de grupo).
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
            if (!panel.getBloque().getGrupoId().equals(this.grupoId)) {
                JOptionPane.showMessageDialog(this,
                    "Este bloque pertenece a otro grupo y no puede ser colocado aqui.",
                    "Error de asignacion",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            super.colocarBloque(panel);
        }
    }
}
