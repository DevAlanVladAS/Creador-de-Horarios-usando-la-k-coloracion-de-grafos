package src;
public interface Validador {

    boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB);

    String getTipoConflicto();
}
