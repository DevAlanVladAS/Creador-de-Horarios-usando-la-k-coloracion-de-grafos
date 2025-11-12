public interface PersistenciaDeDatos {
    void guardarHorario(HorarioSemana horario);
    HorarioSemana cargarHorario();
}
