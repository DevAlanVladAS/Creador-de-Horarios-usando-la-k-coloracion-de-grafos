package src;

public class ValidadorPorGrupo implements Validador {
    private String tipoConflicto;

    @Override
    public boolean esValido(BloqueHorario b1, BloqueHorario b2) {
        // A group cannot attend two classes at once.
        // Check if the blocks are for the same group on the same day.
        if (b1.getGrupo().equals(b2.getGrupo()) && b1.getDia().equals(b2.getDia())) {
            // Check if the time periods overlap.
            if (b1.getHoraInicio().isBefore(b2.getHoraFin()) && b2.getHoraInicio().isBefore(b1.getHoraFin())) {
                this.tipoConflicto = String.format(
                    "Conflicto de Grupo: El grupo %s tiene un choque de horarios el %s.",
                    b1.getGrupo().getId(), b1.getDia()
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
