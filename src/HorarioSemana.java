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


    // Constructor
    public HorarioSemana() {
        this.diasSemana = new ArrayList<>();
        this.bloquesSinAsignar = new ArrayList<>();
        this.asignaciones = new HashMap<>();
    }

    /**
     * Inicializa la semana con una lista de días estándar.
     * Si ya hay días, no hace nada.
     * @param nombresDias Lista de nombres de los días a crear.
     */
    public void inicializarDias(List<String> nombresDias) {
        if (diasSemana.isEmpty() && nombresDias != null) {
            nombresDias.forEach(nombre -> agregarDia(new HorarioDia(nombre)));
        }
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


    public void agregarBloqueEnDia(String diaDestino, BloqueHorario bloque) {
        HorarioDia horarioDiaDestino = diasSemana.stream()
                .filter(d -> d.getDia().equalsIgnoreCase(diaDestino))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Día no encontrado: " + diaDestino));

        String diaAnterior = asignaciones.get(bloque.getId());

        // --- PASO 1: Limpieza exhaustiva de la posición anterior del bloque ---
        // Se elimina de la lista de "sin asignar" y de CUALQUIER día en el que pudiera estar.
        // Esto crea un "estado limpio" y previene duplicados incluso si 'asignaciones' está desincronizado.
        bloquesSinAsignar.removeIf(b -> b.getId().equals(bloque.getId()));
        for (HorarioDia dia : diasSemana) {
            dia.eliminar(bloque); // Intenta eliminar el bloque de cada día.
        }

        // --- PASO 2: Intentar agregar el bloque a su nuevo día ---
        boolean agregadoConExito = horarioDiaDestino.agregar(bloque);

        // --- PASO 3: Actualizar estado y notificar basado en el resultado ---
        if (agregadoConExito) {
            // Si tuvo éxito, se actualiza la asignación definitiva.
            asignaciones.put(bloque.getId(), diaDestino);
            
            if (diaAnterior == null) {
                notifyChange(EventoSemana.BLOQUE_ASIGNADO, bloque, null, diaDestino);
            } else {
                notifyChange(EventoSemana.BLOQUE_MOVIDO, bloque, diaAnterior, diaDestino);
            }
        } else {
            // Si falla (ej. por solapamiento), se asegura que el bloque termine en "sin asignar".
            if (!bloquesSinAsignar.contains(bloque)) {
                bloquesSinAsignar.add(bloque);
            }
            // Se elimina cualquier asignación para evitar un estado inconsistente.
            asignaciones.remove(bloque.getId());

            // Se notifica que fue desasignado, ya que no se pudo colocar en el destino.
            if (diaAnterior != null) {
                notifyChange(EventoSemana.BLOQUE_DESASIGNADO, bloque, diaAnterior, null);
            }
        }
    }

    public void agregarBloqueSinAsignar(BloqueHorario bloque) {
        String diaAnterior = asignaciones.remove(bloque.getId());
        // Limpia el bloque de cualquier día en el que pudiera estar para evitar duplicados.
        for (HorarioDia hd : diasSemana) {
            hd.eliminar(bloque);
        }
        
        bloquesSinAsignar.removeIf(b -> b.getId().equals(bloque.getId()));
        bloquesSinAsignar.add(bloque);
        
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
