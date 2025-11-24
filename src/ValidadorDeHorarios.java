package src;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ejecuta las validaciones (unitarias y de conflicto) sobre un horario semanal.
 */
public class ValidadorDeHorarios {

    private final List<Validador> validadoresDeConflicto;
    private final List<UnaryValidator> validadoresUnitarios;

    /** Inicializa validadores de reglas de negocio (conflictos y unitarios). */
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
     * Ejecuta validaciones unitarias y de conflicto sobre el horario dado.
     * @param horario horario semanal a validar
     * @return lista de resultados (errores y advertencias)
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
     * @return true si hay conflicto de tipo ERROR
     */
    public boolean hayConflictoDirecto(BloqueHorario a, BloqueHorario b) {
        // Si no hay información de día/hora, considerar conflicto potencial cuando comparten recursos,
        // para que la gráfica incluya la arista y el planificador no solape profesores/salones/grupos.
        boolean sinTiempo = a.getDia() == null || b.getDia() == null ||
                a.getHoraInicio() == null || a.getHoraFin() == null ||
                b.getHoraInicio() == null || b.getHoraFin() == null;

        if (sinTiempo) {
            if ((a.getProfesorId() != null && a.getProfesorId().equals(b.getProfesorId())) ||
                (a.getSalonId() != null && a.getSalonId().equals(b.getSalonId())) ||
                (a.getGrupoId() != null && a.getGrupoId().equals(b.getGrupoId()))) {
                return true;
            }
        }

        for (Validador validador : validadoresDeConflicto) {
            List<ResultadoValidacion> resultados = validador.validar(a, b, null);
            for (ResultadoValidacion resultado : resultados) {
                if (resultado.getSeveridad() == ResultadoValidacion.Severidad.ERROR) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Determina si dos bloques se solapan en el tiempo en el mismo dia. */
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
