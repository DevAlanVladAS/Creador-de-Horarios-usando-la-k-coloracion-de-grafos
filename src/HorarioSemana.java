package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Contiene los dias de un horario y los bloques sin asignar.
 * Notifica cambios estructurales a listeners (composite de HorarioDia/BloqueHorario).
 */
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
         * @param diaOrigen Dia de origen (para movimientos)
         * @param diaDestino Dia de destino (para movimientos/asignaciones)
         */
        void onEstructuraCambiada(EventoSemana tipoEvento, BloqueHorario bloque, 
                                 String diaOrigen, String diaDestino);
    }

    /**
     * Tipos de eventos estructurales en la semana.
     */
    public enum EventoSemana {
        BLOQUE_ASIGNADO,
        BLOQUE_DESASIGNADO,
        BLOQUE_MOVIDO,
        BLOQUE_AGREGADO,
        BLOQUE_ELIMINADO,
        DIA_AGREGADO,
        DIA_ELIMINADO
    }

    private List<HorarioSemanaChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        return listeners;
    }

    /**
     * Registra un listener de cambios estructurales.
     */
    public void addListener(HorarioSemanaChangeListener listener) {
        if (!getListeners().contains(listener)) {
            getListeners().add(listener);
        }
    }

    /**
     * Desregistra un listener de cambios estructurales.
     */
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

    /**
     * Crea una semana sin dias ni bloques asignados.
     */
    public HorarioSemana() {
        this.diasSemana = new ArrayList<>();
        this.bloquesSinAsignar = new ArrayList<>();
        this.asignaciones = new HashMap<>();
    }

    /**
     * Inicializa los dias de la semana (si aun no existen).
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
                .orElseThrow(() -> new IllegalArgumentException("Dia no encontrado: " + diaDestino));

        String diaAnterior = asignaciones.get(bloque.getId());

        // Paso 1: limpiar cualquier posicion previa (sin asignar o en otros dias)
        bloquesSinAsignar.removeIf(b -> b.getId().equals(bloque.getId()));
        for (HorarioDia dia : diasSemana) {
            dia.eliminar(bloque);
        }

        // Paso 2: intentar agregar al nuevo dia
        boolean agregadoConExito = horarioDiaDestino.agregar(bloque);

        // Paso 3: actualizar estado/notify segun resultado
        if (agregadoConExito) {
            asignaciones.put(bloque.getId(), diaDestino);
            
            if (diaAnterior == null) {
                notifyChange(EventoSemana.BLOQUE_ASIGNADO, bloque, null, diaDestino);
            } else {
                notifyChange(EventoSemana.BLOQUE_MOVIDO, bloque, diaAnterior, diaDestino);
            }
        } else {
            if (!bloquesSinAsignar.contains(bloque)) {
                bloquesSinAsignar.add(bloque);
            }
            asignaciones.remove(bloque.getId());

            if (diaAnterior != null) {
                notifyChange(EventoSemana.BLOQUE_DESASIGNADO, bloque, diaAnterior, null);
            }
        }
    }

    /**
     * Envia un bloque a la lista de sin asignar y limpia registros previos.
     */
    public void agregarBloqueSinAsignar(BloqueHorario bloque) {
        String diaAnterior = asignaciones.remove(bloque.getId());
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

    /**
     * Asigna un bloque existente a un dia.
     */
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

    /**
     * Remueve un bloque de su dia y lo pasa a sin asignar.
     */
    public void desasignarBloqueADia(String idBloque) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            throw new IllegalArgumentException("Bloque no encontrado: " + idBloque);
        }

        BloqueHorario bloque = contenedor.get();
        agregarBloqueSinAsignar(bloque);
    }

    /**
     * Alias para asignar un bloque a otro dia.
     */
    public void moverBloque(String idBloque, String diaDestino) {
        asignarBloqueADia(idBloque, diaDestino);
    }

    /**
     * Elimina un bloque del sistema (dias y sin asignar).
     */
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

    /**
     * Busca un bloque por ID en dias y sin asignar.
     */
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

    /**
     * Busca un dia por nombre.
     */
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
        throw new IllegalArgumentException("Tipo invalido: " + comp);
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
        throw new IllegalArgumentException("Tipo invalido: " + comp);
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
