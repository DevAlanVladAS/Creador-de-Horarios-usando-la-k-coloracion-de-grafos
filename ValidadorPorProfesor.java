/**
 * Validador que verifica disponibilidad de profesores en días específicos.
 * Usa CatalogoRecursos y la información de disponibilidad en Profesor.
 */
public class ValidadorPorProfesor implements Validador {

    private CatalogoRecursos catalogo;

    public ValidadorPorProfesor(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        // Verificar disponibilidad de profesores en sus días asignados
        if (bloqueA.getDia() != null && !validarProfesor(bloqueA)) {
            return false;
        }
        if (bloqueB.getDia() != null && !validarProfesor(bloqueB)) {
            return false;
        }
        return true;
    }

    /**
     * Valida si el profesor de un bloque está disponible en el día asignado.
     */
    private boolean validarProfesor(BloqueHorario bloque) {
        String profesorId = bloque.getProfesorId();
        if (profesorId == null) return true; // Si no tiene id, permitir
        
        Profesor p = catalogo.obtenerProfesorPorId(profesorId);
        if (p == null) return false; // Profesor no encontrado
        
        return p.disponibleEn(bloque.getDia());
    }

    @Override
    public String getTipoConflicto(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        StringBuilder sb = new StringBuilder("Conflicto de disponibilidad de profesor: ");
        if (bloqueA.getDia() != null) {
            Profesor pA = catalogo.obtenerProfesorPorId(bloqueA.getProfesorId());
            if (pA != null && !pA.disponibleEn(bloqueA.getDia())) {
                sb.append(pA.getNombre()).append(" no disponible en ").append(bloqueA.getDia());
            }
        }
        return sb.toString();
    }
}
