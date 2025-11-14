public class ValidadorPorProfesor implements Validador {

    private CatalogoRecursos catalogo;

    public ValidadorPorProfesor(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {

        if (!a.getProfesorId().equals(b.getProfesorId())) {
            return true;
        }

        boolean mismoDia = a.getDia().equals(b.getDia());
        boolean traslape =
                a.getHoraInicio().isBefore(b.getHoraFin()) &&
                a.getHoraFin().isAfter(b.getHoraInicio());

        if (mismoDia && traslape) {
            return false;
        }

        return true;
    }

    @Override
    public String getTipoConflicto() {
        return "Conflicto de Profesor";
    }
}
