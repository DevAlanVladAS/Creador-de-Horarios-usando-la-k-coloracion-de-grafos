import java.util.List;
import java.util.ArrayList;

public class HorarioDia extends HorarioComponent {

    private String dia;
    private List<HorarioComponent> bloques = new ArrayList<>();

    public HorarioDia(String dia) {
        this.dia = dia;
    }

    @Override
    public void agregar(HorarioComponent componente) {
        bloques.add(componente);
    }

    @Override
    public void eliminar(HorarioComponent componente) {
        bloques.remove(componente);
    }

    @Override
    public List<HorarioComponent> getHijos() {
        return bloques;
    }

    @Override
    public void mostrar() {
        System.out.println("Día: " + dia);
        for (HorarioComponent bloque : bloques) {
            bloque.mostrar();
        }
    }
}
