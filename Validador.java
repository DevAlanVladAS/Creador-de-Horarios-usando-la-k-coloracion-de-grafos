public interface Validador {
    public boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB);
    public String getTipoConflicto(BloqueHorario bloqueA, BloqueHorario bloqueB);
}
