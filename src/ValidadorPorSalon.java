package src;
public class ValidadorPorSalon implements Validador {

    private CatalogoRecursos catalogo;

    public ValidadorPorSalon(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {

        String salonA = a.getSalonId();
        String salonB = b.getSalonId();

        // Si alguno no tiene salón asignado, no hay conflicto por salón.
        if (salonA == null || salonB == null) {
            return true;
        }

        if (!salonA.equals(salonB)) {
            return true;
        }

        boolean mismoDia = a.getDia() != null && a.getDia().equals(b.getDia());
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
