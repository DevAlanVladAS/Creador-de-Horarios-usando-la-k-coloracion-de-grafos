import java.util.*;

public class AdaptadorGraficaDeHorarios {

    private final List<BloqueHorario> nodos;
    private final Map<Integer, Set<Integer>> grafo;  // edges por índice en la lista

    public AdaptadorGraficaDeHorarios(List<BloqueHorario> bloques) {
        this.nodos = new ArrayList<>(bloques);
        this.grafo = construirGrafo(bloques);
    }

    private Map<Integer, Set<Integer>> construirGrafo(List<BloqueHorario> bloques) {
        Map<Integer, Set<Integer>> g = new HashMap<>();
        for (int i = 0; i < bloques.size(); i++) g.put(i, new HashSet<>());

        for (int i = 0; i < bloques.size(); i++) {
            for (int j = i + 1; j < bloques.size(); j++) {
                if (hayConflicto(bloques.get(i), bloques.get(j))) {
                    g.get(i).add(j);
                    g.get(j).add(i);
                }
            }
        }
        return g;
    }

    private boolean hayConflicto(BloqueHorario a, BloqueHorario b) {
        // tu regla de conflicto de fase 1
        return a.getMateria().equals(b.getMateria());
    }

    public List<BloqueHorario> getNodos() { return nodos; }
    public Map<Integer, Set<Integer>> getGrafo() { return grafo; }
}
