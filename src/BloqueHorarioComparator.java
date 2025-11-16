package src;
import java.util.Comparator;

public class BloqueHorarioComparator implements Comparator<BloqueHorario> {
    @Override
    public int compare(BloqueHorario b1, BloqueHorario b2) {
        int cmp = b1.getHoraInicio().compareTo(b2.getHoraInicio());
        if (cmp != 0) return cmp;
        cmp = b1.getHoraFin().compareTo(b2.getHoraFin());
        if (cmp != 0) return cmp;
        return b1.getDuracion().compareTo(b2.getDuracion());
    }
}
