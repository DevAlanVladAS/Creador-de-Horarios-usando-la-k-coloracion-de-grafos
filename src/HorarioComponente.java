package src;
import java.util.List;

public interface HorarioComponente {

    public void agregar(HorarioComponente comp);

    public void eliminar(HorarioComponente comp);

    public List<BloqueHorario> getBloques();
    
    public abstract void mostrarInfo();
}
