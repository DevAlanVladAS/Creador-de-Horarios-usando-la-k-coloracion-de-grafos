package src;
public class ValidadorPorDuracion implements Validador {

    private int duracionMaxima;

    public ValidadorPorDuracion(int duracionMaxima) {
        this.duracionMaxima = duracionMaxima;
    }

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {
        int durA = a.getHoraFin().getHour() - a.getHoraInicio().getHour();
        int durB = b.getHoraFin().getHour() - b.getHoraInicio().getHour();

        if (durA > duracionMaxima || durB > duracionMaxima) return false;

        return true;
    }

    @Override
    public String getTipoConflicto() {
        return "Duración máxima excedida";
    }
}
