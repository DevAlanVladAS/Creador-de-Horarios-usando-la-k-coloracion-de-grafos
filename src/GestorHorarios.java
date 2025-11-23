package src;

import java.time.LocalTime;
import java.util.*;

/**
 * Gestor centralizado de horarios que actúa como fuente única de verdad.
 * 
 * ARQUITECTURA DEL OBSERVER:
 * - Nivel 1: BloqueHorario.BloqueChangeListener → Escucha cambios en bloques individuales
 * - Nivel 2: HorarioSemana.HorarioSemanaChangeListener → Escucha cambios estructurales
 * - Nivel 3: GestorHorarios.HorarioChangeListener → Notifica a las vistas (PanelHorario, PanelHorarioGrado)
 * - Nivel de Validación: GestorHorarios.ValidationListener → Notifica a la UI sobre resultados de validación.
 * 
 * El gestor actúa como puente: escucha cambios del modelo y notifica a las vistas.
 */
public class GestorHorarios implements 
        BloqueHorario.BloqueChangeListener,
        HorarioSemana.HorarioSemanaChangeListener {
    
    private static GestorHorarios instance;
    
    // Fuente única de verdad: HorarioSemana por cada grupo
    private final Map<String, HorarioSemana> horariosPorGrupo;
    
    // Listeners de nivel superior (vistas de la UI)
    private final List<HorarioChangeListener> listeners;
    
    // Listeners para resultados de validación
    private final List<ValidationListener> validationListeners;
    
    // Controlador para ejecutar validaciones
    private final ControladorValidacion controladorValidacion;

    private GestorHorarios() {
        horariosPorGrupo = new HashMap<>();
        listeners = new ArrayList<>();
        validationListeners = new ArrayList<>();
        controladorValidacion = new ControladorValidacion();
    }
    
    public static GestorHorarios getInstance() {
        if (instance == null) {
            instance = new GestorHorarios();
        }
        return instance;
    }

    // ========== Gestión de Listeners de Validación ==========

    public interface ValidationListener {
        void onValidationFinished(List<ResultadoValidacion> resultados);
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private void notifyValidationListeners(List<ResultadoValidacion> resultados) {
        for (ValidationListener listener : new ArrayList<>(validationListeners)) {
            listener.onValidationFinished(resultados);
        }
    }
    
    // ========== Gestión de HorarioSemana ==========
    
    /**
     * Obtiene o crea el HorarioSemana para un grupo específico.
     * Automáticamente registra listeners.
     */
    public HorarioSemana getHorarioSemana(String grupoId) {
        return horariosPorGrupo.computeIfAbsent(grupoId, k -> {
            HorarioSemana semana = new HorarioSemana();
            semana.inicializarDias(Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"));
            semana.addListener(this); // Escuchar cambios estructurales
            return semana;
        });
    }
    
    /**
     * Establece un HorarioSemana completo para un grupo.
     */
    public void setHorarioSemana(String grupoId, HorarioSemana horarioSemana) {
        // Desregistrar listener del horario anterior si existe
        HorarioSemana anterior = horariosPorGrupo.get(grupoId);
        if (anterior != null) {
            anterior.removeListener(this);
            // Desregistrar listeners de todos los bloques del horario anterior
            desregistrarBloquesDeHorario(anterior);
        }
        
        // Registrar listener en el nuevo horario
        horarioSemana.addListener(this);
        horariosPorGrupo.put(grupoId, horarioSemana);
        
        // Registrar listeners en todos los bloques del nuevo horario
        registrarBloquesDeHorario(horarioSemana);
        
        notifyBloquesChanged(grupoId, TipoCambio.REEMPLAZO_COMPLETO, null);
    }
    
    /**
     * Obtiene todos los bloques de un grupo específico.
     */
    public List<BloqueHorario> getBloquesGrupo(String grupoId) {
        HorarioSemana semana = horariosPorGrupo.get(grupoId);
        if (semana == null) {
            return Collections.emptyList();
        }
        return semana.getBloques();
    }
    
    /**
     * Obtiene todos los bloques de múltiples grupos (para vista de grado).
     */
    public List<BloqueHorario> getBloquesGrado(List<String> grupoIds) {
        List<BloqueHorario> bloques = new ArrayList<>();
        for (String grupoId : grupoIds) {
            bloques.addAll(getBloquesGrupo(grupoId));
        }
        return bloques;
    }
    
    /**
     * Agrega un nuevo bloque a un grupo específico.
     */
    public void agregarBloque(BloqueHorario bloque, String grupoId) {
        HorarioSemana semana = getHorarioSemana(grupoId);
        bloque.addListener(this); // Escuchar cambios en el bloque
        semana.agregar(bloque);
        // La notificación se maneja automáticamente vía onEstructuraCambiada
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
            // La notificación se maneja automáticamente vía onEstructuraCambiada
        }
    }
    
    // ========== Operaciones de Posición ==========
    
    /**
     * Actualiza la posición de un bloque (día y hora).
     * Este es el método principal llamado desde el drag & drop de la UI.
     */
    public void actualizarPosicionBloque(BloqueHorario bloque, String dia, LocalTime horaInicio) {
        // Preservar la duración original aunque el bloque se mande a "sin asignar".
        java.time.Duration duracion = bloque.getDuracion();
        if (duracion == null || duracion.isZero()) {
            // Intentar reconstruir a partir de las horas actuales si existen
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
        
        // Asignar a un día específico o mover a "sin asignar"
        if (dia == null) {
            semana.agregarBloqueSinAsignar(bloque);
        } else {
            semana.agregarBloqueEnDia(dia, bloque);
        }
        
        // Actualizar las horas del bloque
        bloque.actualizarPosicion(dia, horaInicioFinal, horaFin);
        
        // Notificar a los listeners de la UI sobre el cambio de posición
        notifyBloquesChanged(grupoId, TipoCambio.BLOQUE_MODIFICADO, bloque);

        // Ejecutar validación después de cada movimiento y notificar
        List<ResultadoValidacion> resultados = controladorValidacion.validarTodo(semana);
        notifyValidationListeners(resultados);
    }
    
    /**
     * Mueve un bloque a un día específico (sin cambiar las horas).
     */
    public void moverBloqueADia(String bloqueId, String grupoId, String diaDestino) {
        HorarioSemana semana = horariosPorGrupo.get(grupoId);
        if (semana != null) {
            semana.asignarBloqueADia(bloqueId, diaDestino);
        }
    }
    
    // ========== Observer Nivel 1: Escuchar BloqueHorario ==========
    
    @Override
    public void onBloqueChanged(BloqueHorario bloque, String propiedad, 
                               Object valorAnterior, Object valorNuevo) {
        // Determinar si el cambio afecta la visualización
        boolean cambioVisual = propiedad.equals("dia") || 
                              propiedad.equals("horaInicio") || 
                              propiedad.equals("horaFin") ||
                              propiedad.equals("posicion") ||
                              propiedad.equals("materia") ||
                              propiedad.equals("profesorId");
        
        if (cambioVisual) {
            String grupoId = bloque.getGrupoId();
            
            // Si cambió de grupo, notificar a ambos
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
    
    // ========== Observer Nivel 2: Escuchar HorarioSemana ==========
    
    @Override
    public void onEstructuraCambiada(HorarioSemana.EventoSemana tipoEvento, 
                                    BloqueHorario bloque, 
                                    String diaOrigen, String diaDestino) {
        if (bloque == null) return;
        
        String grupoId = bloque.getGrupoId();
        
        // Mapear eventos de HorarioSemana a TipoCambio de GestorHorarios
        TipoCambio tipoCambio = switch (tipoEvento) {
            case BLOQUE_ASIGNADO, BLOQUE_DESASIGNADO -> TipoCambio.BLOQUE_MODIFICADO;
            case BLOQUE_MOVIDO -> TipoCambio.BLOQUE_MOVIDO;
            case BLOQUE_AGREGADO -> TipoCambio.BLOQUE_AGREGADO;
            case BLOQUE_ELIMINADO -> TipoCambio.BLOQUE_ELIMINADO;
            case DIA_AGREGADO, DIA_ELIMINADO -> TipoCambio.ESTRUCTURA_CAMBIADA;
        };
        
        notifyBloquesChanged(grupoId, tipoCambio, bloque);
    }
    
    // ========== Observer Nivel 3: Notificar a las Vistas ==========
    
    public enum TipoCambio {
        BLOQUE_AGREGADO,        // Nuevo bloque añadido
        BLOQUE_ELIMINADO,       // Bloque eliminado
        BLOQUE_MODIFICADO,      // Bloque cambió propiedades (materia, profesor, posición)
        BLOQUE_MOVIDO,          // Bloque se movió entre grupos o días
        REEMPLAZO_COMPLETO,     // Se reemplazó todo el horario
        ESTRUCTURA_CAMBIADA     // Cambio estructural (días agregados/eliminados)
    }
    
    public interface HorarioChangeListener {
        /**
         * Se invoca cuando los bloques de un grupo han cambiado.
         * @param grupoId ID del grupo afectado (null = todos)
         * @param tipoCambio Tipo de cambio ocurrido
         * @param bloqueAfectado Bloque específico afectado (si aplica)
         */
        void onBloquesChanged(String grupoId, TipoCambio tipoCambio, BloqueHorario bloqueAfectado);
    }
    
    public void addListener(HorarioChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(HorarioChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyBloquesChanged(String grupoId, TipoCambio tipoCambio, BloqueHorario bloque) {
        // Crear copia para evitar ConcurrentModificationException
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
    
    // ========== Utilidades de Registro de Listeners ==========
    
    /**
     * Registra listeners en todos los bloques de un horario.
     */
    private void registrarBloquesDeHorario(HorarioSemana horario) {
        for (BloqueHorario bloque : horario.getBloques()) {
            bloque.addListener(this);
        }
    }
    
    /**
     * Desregistra listeners de todos los bloques de un horario.
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
     * Obtiene todos los grupos que tienen horarios.
     */
    public Set<String> getGruposConHorarios() {
        return horariosPorGrupo.keySet();
    }
    
    /**
     * Limpia todos los horarios del sistema.
     */
    public void limpiarTodo() {
        // Desregistrar listeners
        for (HorarioSemana semana : horariosPorGrupo.values()) {
            semana.removeListener(this);
            desregistrarBloquesDeHorario(semana);
        }
        
        horariosPorGrupo.clear();
        notifyBloquesChanged(null, TipoCambio.REEMPLAZO_COMPLETO, null);
    }
    
    /**
     * Limpia el horario de un grupo específico.
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
