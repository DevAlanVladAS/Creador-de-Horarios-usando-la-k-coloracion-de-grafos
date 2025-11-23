package src;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que valida las reglas de los horarios.
 * Compara todos los bloques de un horario entre sí para detectar conflictos.
 */
public class ValidadorDeHorarios {

    private final List<Validador> validadoresDeConflicto;
    private final List<UnaryValidator> validadoresUnitarios;

    /**
     * Constructor que inicializa todos los validadores de reglas de negocio.
     */
    public ValidadorDeHorarios() {
        // Validadores binarios para conflictos (generan ERRORES)
        validadoresDeConflicto = new ArrayList<>();
        validadoresDeConflicto.add(new ValidadorPorProfesor());
        validadoresDeConflicto.add(new ValidadorPorSalon());
        validadoresDeConflicto.add(new ValidadorPorGrupo());

        // Validadores unitarios para preferencias (generan ADVERTENCIAS)
        validadoresUnitarios = new ArrayList<>();
        validadoresUnitarios.add(new ValidadorPorPreferenciaProfesor());
    }

    /**
     * Ejecuta todas las validaciones (unitarias y de conflicto) sobre el horario dado.
     * 
     * @param horario el horario semanal a validar.
     * @return lista de todos los resultados de validación (errores y advertencias).
     */
    public List<ResultadoValidacion> validar(HorarioSemana horario) {
        List<ResultadoValidacion> resultados = new ArrayList<>();
        
        List<BloqueHorario> bloquesAsignados = horario.getBloques().stream()
                .filter(b -> b.getDia() != null && b.getHoraInicio() != null)
                .collect(Collectors.toList());

        // 1. Ejecutar validadores unitarios sobre cada bloque (buscando advertencias)
        for (BloqueHorario bloque : bloquesAsignados) {
            for (UnaryValidator validador : validadoresUnitarios) {
                resultados.addAll(validador.validar(bloque));
            }
        }

        // 2. Ejecutar validadores de conflicto entre pares de bloques (buscando errores)
        for (int i = 0; i < bloquesAsignados.size(); i++) {
            for (int j = i + 1; j < bloquesAsignados.size(); j++) {
                BloqueHorario bloqueA = bloquesAsignados.get(i);
                BloqueHorario bloqueB = bloquesAsignados.get(j);

                if (seSolapanEnTiempo(bloqueA, bloqueB)) {
                    for (Validador validador : validadoresDeConflicto) {
                        resultados.addAll(validador.validar(bloqueA, bloqueB, horario));
                    }
                }
            }
        }
        
        if (resultados.isEmpty()) {
            resultados.add(ResultadoValidacion.ofSuccess("Validación completada: No se detectaron conflictos ni advertencias."));
        }

        return resultados;
    }

    /**
     * Comprueba si existe un conflicto directo de recursos entre dos bloques.
     * Un conflicto directo se considera un ERROR de validación.
     * 
     * @param a Bloque A
     * @param b Bloque B
     * @return true si hay conflicto de tipo ERROR, false en caso contrario.
     */
    public boolean hayConflictoDirecto(BloqueHorario a, BloqueHorario b) {
        for (Validador validador : validadoresDeConflicto) {
            List<ResultadoValidacion> resultados = validador.validar(a, b, null); // Contexto no necesario para construcción de grafo
            // Comprobamos si hay algún resultado que sea un ERROR.
            for (ResultadoValidacion resultado : resultados) {
                if (resultado.getSeveridad() == ResultadoValidacion.Severidad.ERROR) {
                    return true; // Conflicto de tipo ERROR encontrado.
                }
            }
        }
        return false; // No hay conflictos de tipo ERROR.
    }

    /**
     * Determina si dos bloques de horario se solapan en el tiempo en el mismo día.
     * @param a Bloque de horario A.
     * @param b Bloque de horario B.
     * @return true si hay solapamiento, false en caso contrario.
     */
    private boolean seSolapanEnTiempo(BloqueHorario a, BloqueHorario b) {
        // Primero, verificar si son del mismo día. Si no, no pueden solaparse.
        if (a.getDia() == null || b.getDia() == null || !a.getDia().equals(b.getDia())) {
            return false;
        }

        // Los bloques se solapan si el inicio de A es antes del fin de B,
        // Y el inicio de B es antes del fin de A.
        return a.getHoraInicio().isBefore(b.getHoraFin()) && 
               b.getHoraInicio().isBefore(a.getHoraFin());
    }
}
