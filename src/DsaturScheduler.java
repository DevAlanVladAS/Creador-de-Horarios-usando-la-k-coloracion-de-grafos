package src;
import java.util.*;

public class DsaturScheduler {

    private final Map<Integer, Set<Integer>> grafo;
    private final List<BloqueHorario> bloques;

    public DsaturScheduler(Map<Integer, Set<Integer>> grafo, List<BloqueHorario> bloques) {
        this.grafo = grafo;
        this.bloques = bloques;
    }

    /** Devuelve un array colores[i] = color asignado al nodo i */
    public int[] asignar() {

        int n = bloques.size();
        int[] color = new int[n];
        Arrays.fill(color, -1);

        // saturación
        List<Set<Integer>> coloresVecinos = new ArrayList<>();
        for (int i = 0; i < n; i++) coloresVecinos.add(new HashSet<>());

        // primer nodo: mayor grado
        int first = 0;
        int maxDegree = -1;
        for (int i = 0; i < n; i++) {
            int deg = grafo.get(i).size();
            if (deg > maxDegree) {
                maxDegree = deg;
                first = i;
            }
        }

        // asignar color 0 al primero
        color[first] = 0;

        // actualizar saturación
        for (int v : grafo.get(first)) {
            coloresVecinos.get(v).add(0);
        }

        // resto
        for (int k = 1; k < n; k++) {

            // elegir siguiente por saturación
            int siguiente = -1;
            int bestSat = -1;
            int bestDeg = -1;

            for (int i = 0; i < n; i++) {
                if (color[i] != -1) continue;

                int sat = coloresVecinos.get(i).size();
                int deg = grafo.get(i).size();

                if (sat > bestSat ||
                   (sat == bestSat && deg > bestDeg)) {
                    bestSat = sat;
                    bestDeg = deg;
                    siguiente = i;
                }
            }

            if (siguiente == -1) break;

            // elegir menor color disponible
            int c = 0;
            while (coloresVecinos.get(siguiente).contains(c)) {
                c++;
            }

            color[siguiente] = c;

            // propagar saturación
            for (int v : grafo.get(siguiente)) {
                coloresVecinos.get(v).add(c);
            }
        }

        return color;
    }
}
