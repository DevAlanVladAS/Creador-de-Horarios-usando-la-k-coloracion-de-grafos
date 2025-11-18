package src;

public class ValidadorPorProfesor implements Validador {
    private String tipoConflicto;

    @Override
    public boolean esValido(BloqueHorario b1, BloqueHorario b2) {
        // A professor cannot be in two places at once.
        // Check if the blocks are for the same professor on the same day.
        if (b1.getProfesor().equals(b2.getProfesor()) && b1.getDia().equals(b2.getDia())) {
            // Check if the time periods overlap.
            if (b1.getHoraInicio().isBefore(b2.getHoraFin()) && b2.getHoraInicio().isBefore(b1.getHoraFin())) {
                this.tipoConflicto = String.format(
                    "Conflicto de Profesor: El profesor %s tiene un choque de horarios el %s.",
                    b1.getProfesor().getNombre(), b1.getDia()
                );
                return false;
            }
        }
        this.tipoConflicto = null;
        return true;
    }

    @Override
    public String getTipoConflicto() {
        return tipoConflicto;
    }
}
