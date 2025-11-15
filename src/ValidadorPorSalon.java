package src;
public class ValidadorPorSalon implements Validador {

    private CatalogoRecursos catalogo;

    public ValidadorPorSalon(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {

        if (!a.getSalonId().equals(b.getSalonId()))
            return true;

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
        return "Conflicto de Salón";
    }
}
