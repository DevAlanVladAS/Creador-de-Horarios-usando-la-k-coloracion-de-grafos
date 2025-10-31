import java.util.List;

public abstract class HorarioComponent {

    public void agregar(HorarioComponent comp) {
        throw new UnsupportedOperationException();
    }

    public void eliminar(HorarioComponent comp) {
        throw new UnsupportedOperationException();
    }

    public List<HorarioComponent> getHijos() {
        throw new UnsupportedOperationException();
    }
    
    public abstract void mostrar();
}
