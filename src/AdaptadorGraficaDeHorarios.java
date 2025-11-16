
package src;
import java.util.*;

public class AdaptadorGraficaDeHorarios implements GraficaHorario {

    private final Map<String, BloqueHorario> nodos;
    private final Map<String, Set<String>> adyacencias;
    private final CatalogoRecursos catalogo;
    private int numAristas;

    public AdaptadorGraficaDeHorarios(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;
    }

    public AdaptadorGraficaDeHorarios(List<BloqueHorario> bloques, CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;

        // Agregar todos los bloques como nodos
        for (BloqueHorario bloque : bloques) {
            agregarNodo(bloque.getId(), bloque);
        }
    }

    @Override
    public void agregarNodo(String bloqueId, BloqueHorario bloque) {
        if (!nodos.containsKey(bloqueId)) {
            nodos.put(bloqueId, bloque);
            adyacencias.put(bloqueId, new HashSet<>());
        }
    }

    @Override
    public void agregarArista(String bloqueIdA, String bloqueIdB) {
        if (!nodos.containsKey(bloqueIdA) || !nodos.containsKey(bloqueIdB)) {
            throw new IllegalArgumentException("Uno o ambos bloques no existen en la gráfica");
        }

        if (!adyacencias.get(bloqueIdA).contains(bloqueIdB)) {
            adyacencias.get(bloqueIdA).add(bloqueIdB);
            adyacencias.get(bloqueIdB).add(bloqueIdA);
            numAristas++;
        }
    }

    /**
     * Construye las aristas automáticamente basándose en conflictos entre bloques.
     * Dos bloques tienen conflicto si:
     * - Comparten el mismo profesor y sus horarios se solapan
     * - Comparten el mismo salón y sus horarios se solapan
     * - Comparten el mismo grupo y sus horarios se solapan
     */
    public void construirGraficaAutomaticamente() {
        List<String> ids = new ArrayList<>(nodos.keySet());

        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String idA = ids.get(i);
                String idB = ids.get(j);

                BloqueHorario bloqueA = nodos.get(idA);
                BloqueHorario bloqueB = nodos.get(idB);

                if (hayConflicto(bloqueA, bloqueB)) {
                    agregarArista(idA, idB);
                }
            }
        }
    }

    private boolean hayConflicto(BloqueHorario a, BloqueHorario b) {
        // Si no se solapan en tiempo, no hay conflicto
        if (!a.seSolapaCon(b)) {
            return false;
        }

        // Si se solapan en tiempo, verificar si comparten recursos
        boolean mismoProfesor = a.getProfesorId() != null &&
                a.getProfesorId().equals(b.getProfesorId());

        boolean mismoSalon = a.getSalonId() != null &&
                a.getSalonId().equals(b.getSalonId());

        boolean mismoGrupo = a.getGrupoId() != null &&
                a.getGrupoId().equals(b.getGrupoId());

        return mismoProfesor || mismoSalon || mismoGrupo;
    }

    public boolean sonAdyacentes(String bloqueIdA, String bloqueIdB) {
        return adyacencias.containsKey(bloqueIdA) &&
                adyacencias.get(bloqueIdA).contains(bloqueIdB);
    }

    @Override
    public void mostrar() {
        System.out.println("=== Gráfica de Conflictos ===");
        System.out.println("Nodos: " + nodos.size());
        System.out.println("Aristas: " + numAristas);
        System.out.println("\nAdyacencias:");

        for (String id : adyacencias.keySet()) {
            BloqueHorario bloque = nodos.get(id);
            Set<String> vecinos = adyacencias.get(id);

            if (!vecinos.isEmpty()) {
                System.out.println(bloque.getMateria() + " (" + id.substring(0, 8) + "...) -> " +
                        vecinos.size() + " conflictos");
            }
        }
    }

    @Override
    public Map<String, Set<String>> obtenerAdyacencias() {
        return new HashMap<>(adyacencias);
    }

    @Override
    public BloqueHorario obtenerBloque(String id) {
        return nodos.get(id);
    }

    @Override
    public int obtenerNumeroNodos() {
        return nodos.size();
    }

    @Override
    public int obtenerNumeroAristas() {
        return numAristas;
    }

    public List<BloqueHorario> obtenerTodosLosBloques() {
        return new ArrayList<>(nodos.values());
    }

    public Map<String, Double> obtenerEstadisticas() {
        Map<String, Double> stats = new HashMap<>();

        // Calcular grado promedio
        double gradoTotal = 0;
        for (Set<String> vecinos : adyacencias.values()) {
            gradoTotal += vecinos.size();
        }
        double gradoPromedio = nodos.isEmpty() ? 0 : gradoTotal / nodos.size();

        // Calcular densidad
        int n = nodos.size();
        double maxAristas = n * (n - 1) / 2.0;
        double densidad = maxAristas == 0 ? 0 : numAristas / maxAristas;

        stats.put("grado_promedio", gradoPromedio);
        stats.put("densidad", densidad);

        return stats;
    }

    public CatalogoRecursos getCatalogo() {
        return catalogo;
    }

    /**
     * Aplica k-coloración usando algoritmo greedy (DSatur simplificado).
     * Retorna un arreglo donde el índice corresponde al bloque y el valor es el color (día).
     */
    public Map<String, Integer> colorear() {
        Map<String, Integer> colores = new HashMap<>();
        List<String> ids = new ArrayList<>(nodos.keySet());

        if (ids.isEmpty()) {
            return colores;
        }

        // Ordenar por grado (más conexiones primero)
        ids.sort((a, b) -> Integer.compare(
                adyacencias.get(b).size(),
                adyacencias.get(a).size()
        ));

        // Asignar color al primer nodo
        colores.put(ids.get(0), 0);

        // Colorear el resto
        for (int i = 1; i < ids.size(); i++) {
            String idActual = ids.get(i);
            Set<String> vecinos = adyacencias.get(idActual);

            // Encontrar colores usados por vecinos
            Set<Integer> coloresUsados = new HashSet<>();
            for (String vecino : vecinos) {
                if (colores.containsKey(vecino)) {
                    coloresUsados.add(colores.get(vecino));
                }
            }

            // Asignar el primer color disponible
            int colorAsignado = 0;
            while (coloresUsados.contains(colorAsignado)) {
                colorAsignado++;
            }

            colores.put(idActual, colorAsignado);
        }

        return colores;
    }
}