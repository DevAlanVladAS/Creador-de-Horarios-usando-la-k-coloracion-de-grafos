
package src;
import java.util.*;

public class AdaptadorGraficaDeHorarios implements GraficaHorario {

    private final Map<String, BloqueHorario> nodos;
    private final Map<String, Set<String>> adyacencias;
    private final CatalogoRecursos catalogo;
    private final ValidadorDeHorarios validador;
    private int numAristas;

    public AdaptadorGraficaDeHorarios(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;
        this.validador = new ValidadorDeHorarios();
    }

    public AdaptadorGraficaDeHorarios(List<BloqueHorario> bloques, CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;
        this.validador = new ValidadorDeHorarios();

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
        // Lógica de solapamiento de tiempo.
        boolean seSolapanEnTiempo;
        if (a.getHoraInicio() == null || a.getHoraFin() == null || b.getHoraInicio() == null || b.getHoraFin() == null) {
            // Si las horas no están definidas, se asume que hay un posible conflicto de tiempo
            // para que se evalúen los recursos. Esto es crucial para la fase de coloración
            // antes de la asignación de horas.
            seSolapanEnTiempo = true;
        } else {
            // Lógica de solapamiento: A empieza antes de que B termine Y B empieza antes de que A termine.
            seSolapanEnTiempo = a.getHoraInicio().isBefore(b.getHoraFin()) && b.getHoraInicio().isBefore(a.getHoraFin());
        }

        // Si no se solapan en tiempo, no puede haber conflicto.
        if (!seSolapanEnTiempo) {
            return false;
        }

        // Si se solapan, delegar la validación de recursos al motor centralizado.
        // Esto incluye validación de Profesor, Salón y Grupo.
        return validador.hayConflictoDirecto(a, b);
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

    public ValidadorDeHorarios getValidador() {
        return validador;
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

    /**
     * Aplica k-coloración usando el algoritmo DSatur.
     * Retorna un mapa donde la clave es el ID del bloque y el valor es el color (día).
     */
    public Map<String, Integer> colorearConDSatur() {
        Map<String, Integer> colores = new HashMap<>();
        if (nodos.isEmpty()) {
            return colores;
        }

        // Mapa de saturación: id -> número de colores distintos en sus vecinos
        Map<String, Integer> saturacion = new HashMap<>();
        // Mapa de grado: id -> número de vecinos no coloreados
        Map<String, Integer> grado = new HashMap<>();
        List<String> nodosNoColoreados = new ArrayList<>(nodos.keySet());

        for (String id : nodosNoColoreados) {
            saturacion.put(id, 0);
            grado.put(id, adyacencias.get(id).size());
        }

        while (colores.size() < nodos.size()) {
            // 1. Encontrar el nodo con máxima saturación. En caso de empate, el de mayor grado.
            // Para garantizar el determinismo, se ordena la lista antes de cada selección.
            // Si hay empates en saturación y grado, siempre se elegirá el mismo nodo (el primero en orden alfabético).
            Collections.sort(nodosNoColoreados);

            String nodoAColorear = null;
            int maxSat = -1;
            int maxGrad = -1;

            for (String id : nodosNoColoreados) {
                int satActual = saturacion.get(id);
                int gradActual = grado.get(id);

                if (satActual > maxSat || (satActual == maxSat && gradActual > maxGrad)) {
                    maxSat = satActual;
                    maxGrad = gradActual;
                    nodoAColorear = id;
                }
            }

            if (nodoAColorear == null) break; // Todos coloreados

            // 2. Encontrar el color más pequeño posible para este nodo
            Set<Integer> coloresDeVecinos = new HashSet<>();
            for (String vecinoId : adyacencias.get(nodoAColorear)) {
                if (colores.containsKey(vecinoId)) {
                    coloresDeVecinos.add(colores.get(vecinoId));
                }
            }

            int colorAsignado = 0;
            while (coloresDeVecinos.contains(colorAsignado)) {
                colorAsignado++;
            }

            // 3. Asignar color y actualizar estructuras
            colores.put(nodoAColorear, colorAsignado);
            nodosNoColoreados.remove(nodoAColorear);

            // 4. Actualizar la saturación de los vecinos
            for (String vecinoId : adyacencias.get(nodoAColorear)) {
                if (!colores.containsKey(vecinoId)) { // Si el vecino no está coloreado
                    // Recalcular la saturación del vecino
                    Set<Integer> coloresDeVecinosDelVecino = new HashSet<>();
                    for (String vecinoDelVecinoId : adyacencias.get(vecinoId)) {
                        if (colores.containsKey(vecinoDelVecinoId)) {
                            coloresDeVecinosDelVecino.add(colores.get(vecinoDelVecinoId));
                        }
                    }
                    saturacion.put(vecinoId, coloresDeVecinosDelVecino.size());
                }
            }
        }

        return colores;
    }
}