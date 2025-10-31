import java.util.List;
import java.util.ArrayList;

public class HorarioSemana extends HorarioComponent {
    private List<HorarioComponent> horariosPorDia = new ArrayList<>();

    @Override
    public void agregar(HorarioComponent componente) {
        horariosPorDia.add(componente);
    }

    @Override
    public void eliminar(HorarioComponent componente) {
        horariosPorDia.remove(componente);
    }

    @Override
    public List<HorarioComponent> getHijos() {
        return horariosPorDia;
    }

    @Override
    public void mostrar() {
        System.out.println("Horario de la semana:");
        for (HorarioComponent horario : horariosPorDia) {
            horario.mostrar();
        }
    }
}
