import java.util.List;

public class ValidadorPorDia implements Validador {

    private List<String> diasPermitidos;

    public ValidadorPorDia(List<String> diasPermitidos) {
        this.diasPermitidos = diasPermitidos;
    }

    @Override
    public boolean esValido(BloqueHorario a, BloqueHorario b) {
        if (!diasPermitidos.contains(a.getDia())) return false;
        if (!diasPermitidos.contains(b.getDia())) return false;

        return true;
    }

    @Override
    public String getTipoConflicto() {
        return "Día no permitido";
    }
}
