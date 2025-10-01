import java.util.List;
import java.util.ArrayList;

public class ValidadorHorarios extends HorarioComponent{

    private HorarioComponent componente;

    public static boolean validarConflictos(HorarioComponent semana) {
        for (HorarioComponent dia : semana.getHijos()) {
            List<BloqueHorario> bloques = extraerBloques(dia);
            if (hayConflictos(bloques)) {
                return false;
            }
        }
        return true;
    }

    private static List<BloqueHorario> extraerBloques(HorarioComponent comp) {
        List<BloqueHorario> lista = new ArrayList<>();
        if (comp instanceof BloqueHorario) {
            lista.add((BloqueHorario) comp);
        } else {
            for (HorarioComponent hijo : comp.getHijos()) {
                lista.addAll(extraerBloques(hijo));
            }
        }
        return lista;
    }

    private static boolean hayConflictos(List<BloqueHorario> bloques) {
        for (int i = 0; i < bloques.size(); i++) {
            for (int j = i + 1; j < bloques.size(); j++) {
                if (seTraslapan(bloques.get(i), bloques.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean seTraslapan(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        
        if (!bloqueA.getDia().equals(bloqueB.getDia())) return false;

        boolean mismaAula = 
        bloqueA.getAula().equals(bloqueB.getAula()) ||
        bloqueA.getGrupo().equals(bloqueB.getGrupo())||
        bloqueA.getProfesor().equals(bloqueB.getProfesor());
        
        if (!mismaAula) return false;

        return !(bloqueA.getHoraFin().compareTo(bloqueB.getHoraInicio()) <= 0 ||
                 bloqueB.getHoraFin().compareTo(bloqueA.getHoraInicio()) <= 0);
    }

    @Override
    public void mostrar() {

        if(validarConflictos(componente)){
            componente.mostrar();
        }
        System.out.println("Horario no valido");
    }
}
