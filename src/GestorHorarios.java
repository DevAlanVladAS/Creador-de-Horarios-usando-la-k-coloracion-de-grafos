package src;

import java.time.LocalTime;
import java.util.*;

/**
 * Gestor centralizado de horarios (singleton) y puente de notificaciones entre
 * modelo (BloqueHorario/HorarioSemana) y vistas/validacion.
 */
public class GestorHorarios implements 
        BloqueHorario.BloqueChangeListener,
        HorarioSemana.HorarioSemanaChangeListener {
    
    private static GestorHorarios instance;
    
    private final Map<String, HorarioSemana> horariosPorGrupo;
    private final List<HorarioChangeListener> listeners;
    private final List<ValidationListener> validationListeners;
    private final ControladorValidacion controladorValidacion;

    private GestorHorarios() {
        horariosPorGrupo = new HashMap<>();
        listeners = new ArrayList<>();
        validationListeners = new ArrayList<>();
        controladorValidacion = new ControladorValidacion();
    }
    
    /**
     * Obtiene la instancia unica del gestor.
     */
    public static GestorHorarios getInstance() {
        if (instance == null) {
            instance = new GestorHorarios();
        }
        return instance;
    }

    // ========== Listeners de Validacion ==========

    public interface ValidationListener {
        void onValidationFinished(List<ResultadoValidacion> resultados);
    }

    /**
     * Registra un listener de validacion.
     */
    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    /**
     * Desregistra un listener de validacion.
     */
    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private void notifyValidationListeners(List<ResultadoValidacion> resultados) {
        for (ValidationListener listener : new ArrayList<>(validationListeners)) {
            listener.onValidationFinished(resultados);
        }
    }
    
    // ========== Gestion de HorarioSemana ==========
    
    /**
     * Obtiene o crea el HorarioSemana para un grupo, registrando listeners.
     */
    public HorarioSemana getHorarioSemana(String grupoId) {
        return horariosPorGrupo.computeIfAbsent(grupoId, k -> {
            HorarioSemana semana = new HorarioSemana();
            semana.inicializarDias(Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes"));
            semana.addListener(this);
            return semana;
        });
    }
    
    /**
     * Reemplaza el HorarioSemana de un grupo, registrando/desregistrando listeners.
     */
    public void setHorarioSemana(String grupoId, HorarioSemana horarioSemana) {
        HorarioSemana anterior = horariosPorGrupo.get(grupoId);
        if (anterior != null) {
            anterior.removeListener(this);
            desregistrarBloquesDeHorario(anterior);
        }
        
        horarioSemana.addListener(this);
        horariosPorGrupo.put(grupoId, horarioSemana);
        
        registrarBloquesDeHorario(horarioSemana);
        
        notifyBloquesChanged(grupoId, TipoCambio.REEMPLAZO_COMPLETO, null);
    }
    
    /**
     * Bloques de un grupo (lista viva del horario).
     */
    public List<BloqueHorario> getBloquesGrupo(String grupoId) {
        HorarioSemana semana = horariosPorGrupo.get(grupoId);
        if (semana == null) {
            return Collections.emptyList();
        }
        return semana.getBloques();
    }
    
    /**
     * Bloques combinados de varios grupos (vista grado).
     */
    public List<BloqueHorario> getBloquesGrado(List<String> grupoIds) {
        List<BloqueHorario> bloques = new ArrayList<>();
        for (String grupoId : grupoIds) {
            bloques.addAll(getBloquesGrupo(grupoId));
        }
        return bloques;
    }
    
    /**
     * Agrega un bloque a un grupo y registra listener en el bloque.
     */
    public void agregarBloque(BloqueHorario bloque, String grupoId) {
        HorarioSemana semana = getHorarioSemana(grupoId);
        bloque.addListener(this);
        semana.agregar(bloque);
    }
    
    /**
     * Elimina un bloque del sistema.
     */
    public void eliminarBloque(BloqueHorario bloque) {
        String grupoId = bloque.getGrupoId();
        HorarioSemana semana = horariosPorGrupo.get(grupoId);
        if (semana != null) {
            bloque.removeListener(this);
            semana.eliminarBloque(bloque.getId());
        }
    }
    
    // ========== Operaciones de Posicion ==========
    
    /**
     * Actualiza la posicion (dia/hora) del bloque y dispara validacion/notificacion.
     */
    public void actualizarPosicionBloque(BloqueHorario bloque, String dia, LocalTime horaInicio) {
        java.time.Duration duracion = bloque.getDuracion();
        if (duracion == null || duracion.isZero()) {
            if (bloque.getHoraInicio() != null && bloque.getHoraFin() != null) {
                duracion = java.time.Duration.between(bloque.getHoraInicio(), bloque.getHoraFin());
            }
        }

        LocalTime horaInicioFinal = horaInicio != null ? horaInicio : bloque.getHoraInicio();
        LocalTime horaFin = (horaInicioFinal != null && duracion != null && !duracion.isZero())
            ? horaInicioFinal.plus(duracion)
            : bloque.getHoraFin();
        
        String grupoId = bloque.getGrupoId();
        HorarioSemana semana = getHorarioSemana(grupoId);
        
        if (dia == null) {
            semana.agregarBloqueSinAsignar(bloque);
        } else {
            semana.agregarBloqueEnDia(dia, bloque);
        }
        
        bloque.actualizarPosicion(dia, horaInicioFinal, horaFin);
        
        notifyBloquesChanged(grupoId, TipoCambio.BLOQUE_MODIFICADO, bloque);

        List<ResultadoValidacion> resultados = controladorValidacion.validarTodo(semana);
        notifyValidationListeners(resultados);
    }
    
    /**
     * Mueve un bloque a un dia especifico sin cambiar horas.
     */
    public void moverBloqueADia(String bloqueId, String grupoId, String diaDestino) {
        HorarioSemana semana = horariosPorGrupo.get(grupoId);
        if (semana != null) {
            semana.asignarBloqueADia(bloqueId, diaDestino);
        }
    }
    
    // ========== Observer Nivel 1: BloqueHorario ==========
    
    @Override
    public void onBloqueChanged(BloqueHorario bloque, String propiedad, 
                               Object valorAnterior, Object valorNuevo) {
        boolean cambioVisual = propiedad.equals("dia") || 
                              propiedad.equals("horaInicio") || 
                              propiedad.equals("horaFin") ||
                              propiedad.equals("posicion") ||
                              propiedad.equals("materia") ||
                              propiedad.equals("profesorId");
        
        if (cambioVisual) {
            String grupoId = bloque.getGrupoId();
            
            if (propiedad.equals("grupoId")) {
                String grupoAnterior = (String) valorAnterior;
                String grupoNuevo = (String) valorNuevo;
                
                if (grupoAnterior != null) {
                    notifyBloquesChanged(grupoAnterior, TipoCambio.BLOQUE_MOVIDO, bloque);
                }
                if (grupoNuevo != null) {
                    notifyBloquesChanged(grupoNuevo, TipoCambio.BLOQUE_MOVIDO, bloque);
                }
            } else {
                notifyBloquesChanged(grupoId, TipoCambio.BLOQUE_MODIFICADO, bloque);
            }
        }
    }
    
    // ========== Observer Nivel 2: HorarioSemana ==========
    
    @Override
    public void onEstructuraCambiada(HorarioSemana.EventoSemana tipoEvento, 
                                    BloqueHorario bloque, 
                                    String diaOrigen, String diaDestino) {
        if (bloque == null) return;
        
        String grupoId = bloque.getGrupoId();
        
        TipoCambio tipoCambio = switch (tipoEvento) {
            case BLOQUE_ASIGNADO, BLOQUE_DESASIGNADO -> TipoCambio.BLOQUE_MODIFICADO;
            case BLOQUE_MOVIDO -> TipoCambio.BLOQUE_MOVIDO;
            case BLOQUE_AGREGADO -> TipoCambio.BLOQUE_AGREGADO;
            case BLOQUE_ELIMINADO -> TipoCambio.BLOQUE_ELIMINADO;
            case DIA_AGREGADO, DIA_ELIMINADO -> TipoCambio.ESTRUCTURA_CAMBIADA;
        };
        
        notifyBloquesChanged(grupoId, tipoCambio, bloque);
    }
    
    // ========== Observer Nivel 3: Vistas ==========
    
    public enum TipoCambio {
        BLOQUE_AGREGADO,
        BLOQUE_ELIMINADO,
        BLOQUE_MODIFICADO,
        BLOQUE_MOVIDO,
        REEMPLAZO_COMPLETO,
        ESTRUCTURA_CAMBIADA
    }
    
    public interface HorarioChangeListener {
        /**
         * Se invoca cuando los bloques de un grupo han cambiado.
         * @param grupoId ID del grupo afectado (null = todos)
         * @param tipoCambio Tipo de cambio ocurrido
         * @param bloqueAfectado Bloque especifico afectado (si aplica)
         */
        void onBloquesChanged(String grupoId, TipoCambio tipoCambio, BloqueHorario bloqueAfectado);
    }
    
    /**
     * Registra un listener de cambios de horario.
     */
    public void addListener(HorarioChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Desregistra un listener de cambios de horario.
     */
    public void removeListener(HorarioChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyBloquesChanged(String grupoId, TipoCambio tipoCambio, BloqueHorario bloque) {
        List<HorarioChangeListener> listenersCopy = new ArrayList<>(listeners);
        
        for (HorarioChangeListener listener : listenersCopy) {
            try {
                listener.onBloquesChanged(grupoId, tipoCambio, bloque);
            } catch (Exception e) {
                System.err.println("Error notificando listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // ========== Utilidades de Registro ==========
    
    /**
     * Registra al gestor como listener de todos los bloques de un horario.
     */
    private void registrarBloquesDeHorario(HorarioSemana horario) {
        for (BloqueHorario bloque : horario.getBloques()) {
            bloque.addListener(this);
        }
    }
    
    /**
     * Quita al gestor como listener de todos los bloques de un horario.
     */
    private void desregistrarBloquesDeHorario(HorarioSemana horario) {
        for (BloqueHorario bloque : horario.getBloques()) {
            bloque.removeListener(this);
        }
    }
    
    // ========== Utilidades ==========
    
    /**
     * Busca un bloque por ID en todos los grupos.
     */
    public Optional<BloqueHorario> buscarBloquePorId(String bloqueId) {
        for (HorarioSemana semana : horariosPorGrupo.values()) {
            Optional<BloqueHorario> resultado = semana.obtenerBloquePorID(bloqueId);
            if (resultado.isPresent()) {
                return resultado;
            }
        }
        return Optional.empty();
    }
    
    /**
     * IDs de grupos que tienen horarios cargados.
     */
    public Set<String> getGruposConHorarios() {
        return horariosPorGrupo.keySet();
    }
    
    /**
     * Limpia todos los horarios del sistema.
     */
    public void limpiarTodo() {
        for (HorarioSemana semana : horariosPorGrupo.values()) {
            semana.removeListener(this);
            desregistrarBloquesDeHorario(semana);
        }
        
        horariosPorGrupo.clear();
        notifyBloquesChanged(null, TipoCambio.REEMPLAZO_COMPLETO, null);
    }
    
    /**
     * Limpia el horario de un grupo especifico.
     */
    public void limpiarGrupo(String grupoId) {
        HorarioSemana semana = horariosPorGrupo.remove(grupoId);
        if (semana != null) {
            semana.removeListener(this);
            desregistrarBloquesDeHorario(semana);
            notifyBloquesChanged(grupoId, TipoCambio.REEMPLAZO_COMPLETO, null);
        }
    }
}
