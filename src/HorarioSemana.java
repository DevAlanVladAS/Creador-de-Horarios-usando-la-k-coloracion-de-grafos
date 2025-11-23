package src;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class HorarioSemana implements HorarioComponente, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<HorarioDia> diasSemana;                     
    private List<BloqueHorario> bloquesSinAsignar;           
    private Map<String, String> asignaciones;                

    private transient List<HorarioSemanaChangeListener> listeners;

    public interface HorarioSemanaChangeListener {
        /**
         * Se invoca cuando la estructura de la semana cambia.
         * @param tipoEvento Tipo de cambio ocurrido
         * @param bloque Bloque involucrado (si aplica)
         * @param diaOrigen DÃƒÆ’Ã‚Â­a de origen (para movimientos)
         * @param diaDestino DÃƒÆ’Ã‚Â­a de destino (para movimientos/asignaciones)
         */
        void onEstructuraCambiada(EventoSemana tipoEvento, BloqueHorario bloque, 
                                 String diaOrigen, String diaDestino);
    }

    /**
     * Tipos de eventos estructurales en la semana.
     */
    public enum EventoSemana {
        BLOQUE_ASIGNADO,        // Bloque movido de "sin asignar" a un dí­a
        BLOQUE_DESASIGNADO,     // Bloque movido de un día a "sin asignar"
        BLOQUE_MOVIDO,          // Bloque movido entre di­as
        BLOQUE_AGREGADO,        // Nuevo bloque agregado al sistema
        BLOQUE_ELIMINADO,       // Bloque eliminado del sistema
        DIA_AGREGADO,           // Nuevo dí­a agregado
        DIA_ELIMINADO           // Dí­a eliminado
    }

    private List<HorarioSemanaChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        return listeners;
    }

    public void addListener(HorarioSemanaChangeListener listener) {
        if (!getListeners().contains(listener)) {
            getListeners().add(listener);
        }
    }

    public void removeListener(HorarioSemanaChangeListener listener) {
        getListeners().remove(listener);
    }

    private void notifyChange(EventoSemana evento, BloqueHorario bloque, String diaOrigen, String diaDestino) {
        List<HorarioSemanaChangeListener> listenersCopy = new ArrayList<>(getListeners());
        
        for (HorarioSemanaChangeListener listener : listenersCopy) {
            try {
                listener.onEstructuraCambiada(evento, bloque, diaOrigen, diaDestino);
            } catch (Exception e) {
                System.err.println("Error notificando cambio en semana: " + e.getMessage());
            }
        }
    }


    public HorarioSemana() {
        this.diasSemana = new ArrayList<>();
        this.bloquesSinAsignar = new ArrayList<>();
        this.asignaciones = new HashMap<>();
    }



    public List<HorarioDia> getDiasSemana() {
        return diasSemana;
    }

    public List<BloqueHorario> getBloquesSinAsignar() {
        return new ArrayList<>(bloquesSinAsignar);
    }

    public Optional<String> getDiaAsignado(String idBloque) {
        return Optional.ofNullable(asignaciones.get(idBloque));
    }


    public void agregarDia(HorarioDia dia) {
        if (!diasSemana.contains(dia)) {
            diasSemana.add(dia);
            notifyChange(EventoSemana.DIA_AGREGADO, null, null, dia.getDia());
        }
    }

    public void eliminarDia(HorarioDia dia) {
        if (diasSemana.remove(dia)) {
            List<BloqueHorario> bloquesDelDia = new ArrayList<>(dia.getBloques());
            for (BloqueHorario bloque : bloquesDelDia) {
                agregarBloqueSinAsignar(bloque);
            }
            notifyChange(EventoSemana.DIA_ELIMINADO, null, dia.getDia(), null);
        }
    }


    public void agregarBloqueEnDia(String dia, BloqueHorario bloque) {
        for (HorarioDia horarioDia : diasSemana) {
            if (horarioDia.getDia().equalsIgnoreCase(dia)) {
                String diaAnterior = asignaciones.get(bloque.getId());

                // Si el bloque estaba en otro dí­a, lo eliminamos de allá­ primero.
                if (diaAnterior != null && !diaAnterior.equalsIgnoreCase(dia)) {
                    for (HorarioDia hd : diasSemana) {
                        if (hd.getDia().equalsIgnoreCase(diaAnterior)) {
                            hd.eliminar(bloque);
                            break;
                        }
                    }
                }

                // Intentamos agregar el bloque al dí­a de destino.
                boolean agregadoConExito = horarioDia.agregar(bloque);

                if (agregadoConExito) {
                    // Si se agrega, lo quitamos de la lista de sin asignar y actualizamos su estado.
                    bloquesSinAsignar.remove(bloque);
                    asignaciones.put(bloque.getId(), dia);
                    
                    if (diaAnterior == null) {
                        notifyChange(EventoSemana.BLOQUE_ASIGNADO, bloque, null, dia);
                    } else {
                        notifyChange(EventoSemana.BLOQUE_MOVIDO, bloque, diaAnterior, dia);
                    }
                } else {
                    // Si no se pudo agregar (por traslape), lo movemos a la lista de sin asignar.
                    agregarBloqueSinAsignar(bloque);
                }
                
                return;
            }
        }
        throw new IllegalArgumentException("Día no encontrado: " + dia);
    }

    public void agregarBloqueSinAsignar(BloqueHorario bloque) {
        String diaAnterior = asignaciones.remove(bloque.getId());
        

        if (diaAnterior != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(diaAnterior)) {
                    hd.eliminar(bloque);
                    break;
                }
            }
        }
        
        if (!bloquesSinAsignar.contains(bloque)) {
            bloquesSinAsignar.add(bloque);
        }
        
        bloque.setDia(null);
        
        if (diaAnterior != null) {
            notifyChange(EventoSemana.BLOQUE_DESASIGNADO, bloque, diaAnterior, null);
        }
    }

    public void asignarBloqueADia(String idBloque, String dia) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            throw new IllegalArgumentException("Bloque no encontrado: " + idBloque);
        }

        BloqueHorario bloque = contenedor.get();
        String diaActual = asignaciones.get(idBloque);

        if (diaActual != null && diaActual.equalsIgnoreCase(dia)) {
            return;
        }

        if (diaActual != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(diaActual)) {
                    hd.eliminar(bloque);
                    break;
                }
            }
        } else {
            bloquesSinAsignar.remove(bloque);
        }

        agregarBloqueEnDia(dia, bloque);
    }

    public void desasignarBloqueADia(String idBloque) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            throw new IllegalArgumentException("Bloque no encontrado: " + idBloque);
        }

        BloqueHorario bloque = contenedor.get();
        agregarBloqueSinAsignar(bloque);
    }

    public void moverBloque(String idBloque, String diaDestino) {
        asignarBloqueADia(idBloque, diaDestino);
    }

    public void eliminarBloque(String idBloque) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            return;
        }

        BloqueHorario bloque = contenedor.get();
        String diaActual = asignaciones.remove(idBloque);

        if (diaActual != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(diaActual)) {
                    hd.eliminar(bloque);
                    break;
                }
            }
        } else {
            bloquesSinAsignar.remove(bloque);
        }

        notifyChange(EventoSemana.BLOQUE_ELIMINADO, bloque, diaActual, null);
    }

    public Optional<BloqueHorario> obtenerBloquePorID(String id) {
        for (HorarioDia dia : diasSemana) {
            for (BloqueHorario b : dia.getBloques()) {
                if (b.getId().equals(id)) {
                    return Optional.of(b);
                }
            }
        }

        for (BloqueHorario b : bloquesSinAsignar) {
            if (b.getId().equals(id)) {
                return Optional.of(b);
            }
        }

        return Optional.empty();
    }

    public Optional<HorarioDia> obtenerDiaPorNombre(String nombreDia) {
        return diasSemana.stream()
            .filter(dia -> dia.getDia().equalsIgnoreCase(nombreDia))
            .findFirst();
    }


    @Override
    public void mostrarInfo() {
        for (HorarioDia dia : diasSemana) {
            dia.mostrarInfo();
        }
        if (!bloquesSinAsignar.isEmpty()) {
            System.out.println("Bloques sin asignar: " + bloquesSinAsignar.size());
        }
    }

    @Override
    public boolean agregar(HorarioComponente comp) {
        if (comp instanceof HorarioDia) {
            agregarDia((HorarioDia) comp);
            return true;
        }
        if (comp instanceof BloqueHorario) {
            BloqueHorario bloque = (BloqueHorario) comp;
            agregarBloqueSinAsignar(bloque);
            notifyChange(EventoSemana.BLOQUE_AGREGADO, bloque, null, null);
            return true;
        }
        throw new IllegalArgumentException("Tipo inválido: " + comp);
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (comp instanceof HorarioDia) {
            eliminarDia((HorarioDia) comp);
            return;
        }
        if (comp instanceof BloqueHorario) {
            eliminarBloque(((BloqueHorario) comp).getId());
            return;
        }
        throw new IllegalArgumentException("Tipo inválido: " + comp);
    }

    @Override
    public List<BloqueHorario> getBloques() {
        List<BloqueHorario> todos = new ArrayList<>();
        for (HorarioDia dia : diasSemana) {
            todos.addAll(dia.getBloques());
        }
        todos.addAll(bloquesSinAsignar);
        return todos;
    }
}