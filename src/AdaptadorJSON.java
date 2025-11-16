package src;
public class AdaptadorJSON implements PersistenciaDeDatos {
    private String archivo;

    public AdaptadorJSON(String archivo) {
        this.archivo = archivo;
    }

    @Override
    public void guardarHorario(HorarioSemana horario) {
        // TODO: Implementar serialización JSON
    }

    @Override
    public HorarioSemana cargarHorario() {
        // TODO: Implementar deserialización JSON
        return null;
    }
}
