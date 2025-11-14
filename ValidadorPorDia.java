import java.util.List;

/**
 * Validador que verifica si los bloques están asignados a días permitidos.
 * Usa CatalogoRecursos para validar disponibilidad de profesores en los días asignados.
 * Puede ser usado de dos formas:
 * 1. Con los días del bloque (si están asignados)
 * 2. Con días explícitos pasados como parámetro
 */
public class ValidadorPorDia implements Validador {

    private List<String> diasPermitidos;
    private CatalogoRecursos catalogo;

    public ValidadorPorDia(List<String> diasPermitidos) {
        this.diasPermitidos = diasPermitidos;
        this.catalogo = null;
    }

    public ValidadorPorDia(List<String> diasPermitidos, CatalogoRecursos catalogo) {
        this.diasPermitidos = diasPermitidos;
        this.catalogo = catalogo;
    }

    @Override
    public boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        // Usa los días asignados en los bloques
        String diaA = bloqueA.getDia();
        String diaB = bloqueB.getDia();
        
        if (diaA == null || diaB == null) {
            // Si no están asignados, no podemos validar
            return true; // Permitir por defecto (bloques sin asignar)
        }
        
        // Verificar que los días estén en la lista de permitidos
        if (!diasPermitidos.contains(diaA) || !diasPermitidos.contains(diaB)) {
            return false;
        }
        
        // Validar disponibilidad de recursos en esos días si se tiene catálogo
        if (catalogo != null) {
            if (!validarDisponibilidadProfesor(bloqueA, diaA)) return false;
            if (!validarDisponibilidadProfesor(bloqueB, diaB)) return false;
        }
        
        return true;
    }

    /**
     * Valida si el profesor de un bloque está disponible en el día especificado.
     */
    private boolean validarDisponibilidadProfesor(BloqueHorario bloque, String dia) {
        String profesorId = bloque.getProfesorId();
        if (profesorId == null) return true; // Si no tiene id, permitir
        
        Profesor p = catalogo.obtenerProfesorPorId(profesorId );
        if (p == null) return false; // Profesor no encontrado
        
        return p.disponibleEn(dia);
    }

    /**
     * Version que acepta días explícitos (útil para pre-validación por UI).
     */
    public boolean esValido(BloqueHorario bloqueA, String diaA, BloqueHorario bloqueB, String diaB) {
        if (diaA == null || diaB == null) return true;
        
        // Verificar que los días estén en la lista de permitidos
        if (!diasPermitidos.contains(diaA) || !diasPermitidos.contains(diaB)) {
            return false;
        }
        
        // Validar disponibilidad de recursos si se tiene catálogo
        if (catalogo != null) {
            if (!validarDisponibilidadProfesor(bloqueA, diaA)) return false;
            if (!validarDisponibilidadProfesor(bloqueB, diaB)) return false;
        }
        
        return true;
    }

    @Override
    public String getTipoConflicto(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        return "Conflicto de dia no permitido. Dias permitidos: " + diasPermitidos;
    }
}

