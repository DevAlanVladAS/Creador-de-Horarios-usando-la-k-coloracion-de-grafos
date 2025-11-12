import java.util.ArrayList;
import java.util.List;

public class HorarioDia implements HorarioComponente {
    private String dia;
    private List<BloqueHorario> bloquesHorario;

    public HorarioDia(String dia) {
        this.dia = dia;
        this.bloquesHorario = new ArrayList<BloqueHorario>();
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    @Override
    public void mostrarInfo() {
        System.out.println("Horario del dia: " + dia);
        for (BloqueHorario bloque : bloquesHorario) {
            bloque.mostrarInfo();
        }
    }

    @Override
    public void agregar(HorarioComponente comp) {
        if (comp instanceof BloqueHorario) {
            BloqueHorario b = (BloqueHorario) comp;
            // al agregar a un HorarioDia, asignamos el día al bloque (opcional)
            b.setDia(this.dia);
            bloquesHorario.add(b);
        } else {
            throw new IllegalArgumentException("Solo se pueden agregar BloqueHorario");
        }
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (comp instanceof BloqueHorario) {
            BloqueHorario b = (BloqueHorario) comp;
            boolean removed = bloquesHorario.remove(b);
            if (removed) {
                // al quitar del día, desasignamos el campo dia del bloque
                b.setDia(null);
            }
        } else {
            throw new IllegalArgumentException("Solo se pueden eliminar BloqueHorario");
        }
    }

    @Override
    public List<BloqueHorario> getBloques() {
        return bloquesHorario;
    }
}