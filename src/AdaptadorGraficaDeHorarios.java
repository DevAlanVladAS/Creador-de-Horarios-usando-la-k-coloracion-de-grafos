package src;

import java.util.*;

/**
 * Adapta la lista de bloques a una grafica de conflictos (nodos y aristas)
 * para que los algoritmos de coloracion puedan detectar incompatibilidades.
 */
public class AdaptadorGraficaDeHorarios implements GraficaHorario {

    private final Map<String, BloqueHorario> nodos;
    private final Map<String, Set<String>> adyacencias;
    private final CatalogoRecursos catalogo;
    private final ValidadorDeHorarios validador;
    private int numAristas;

    /**
     * Crea una grafica vacia vinculada a un catalogo.
     */
    public AdaptadorGraficaDeHorarios(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;
        this.validador = new ValidadorDeHorarios();
    }

    /**
     * Crea la grafica inicializando todos los nodos con los bloques recibidos.
     */
    public AdaptadorGraficaDeHorarios(List<BloqueHorario> bloques, CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        this.nodos = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.numAristas = 0;
        this.validador = new ValidadorDeHorarios();

        for (BloqueHorario bloque : bloques) {
            agregarNodo(bloque.getId(), bloque);
        }
    }

    /**
     * Registra un bloque como nodo de la grafica si aun no existe.
     */
    @Override
    public void agregarNodo(String bloqueId, BloqueHorario bloque) {
        if (!nodos.containsKey(bloqueId)) {
            nodos.put(bloqueId, bloque);
            adyacencias.put(bloqueId, new HashSet<>());
        }
    }

    /**
     * Conecta dos nodos mediante una arista no dirigida.
     * @throws IllegalArgumentException si alguno de los nodos no existe.
     */
    @Override
    public void agregarArista(String bloqueIdA, String bloqueIdB) {
        if (!nodos.containsKey(bloqueIdA) || !nodos.containsKey(bloqueIdB)) {
            throw new IllegalArgumentException("Uno o ambos bloques no existen en la grafica");
        }

        if (!adyacencias.get(bloqueIdA).contains(bloqueIdB)) {
            adyacencias.get(bloqueIdA).add(bloqueIdB);
            adyacencias.get(bloqueIdB).add(bloqueIdA);
            numAristas++;
        }
    }

    /**
     * Genera aristas automaticamente entre bloques que entran en conflicto
     * (mismo profesor, salon o grupo con solapamiento de horario).
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

    /**
     * Determina si dos bloques tienen conflicto por tiempo y recursos.
     */
    private boolean hayConflicto(BloqueHorario a, BloqueHorario b) {
        boolean seSolapanEnTiempo;
        if (a.getHoraInicio() == null || a.getHoraFin() == null ||
            b.getHoraInicio() == null || b.getHoraFin() == null) {
            // Con horas indefinidas asumimos posible conflicto para que se coloree separado.
            seSolapanEnTiempo = true;
        } else {
            seSolapanEnTiempo = a.getHoraInicio().isBefore(b.getHoraFin()) &&
                                b.getHoraInicio().isBefore(a.getHoraFin());
        }

        if (!seSolapanEnTiempo) {
            return false;
        }

        return validador.hayConflictoDirecto(a, b);
    }

    /**
     * Indica si dos nodos son vecinos en la grafica.
     */
    public boolean sonAdyacentes(String bloqueIdA, String bloqueIdB) {
        return adyacencias.containsKey(bloqueIdA) &&
               adyacencias.get(bloqueIdA).contains(bloqueIdB);
    }

    /**
     * Muestra en consola un resumen de nodos y conflictos.
     */
    @Override
    public void mostrar() {
        System.out.println("=== Grafica de Conflictos ===");
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

    /**
     * Devuelve una copia del mapa de adyacencias.
     */
    @Override
    public Map<String, Set<String>> obtenerAdyacencias() {
        return new HashMap<>(adyacencias);
    }

    /**
     * Obtiene el bloque asociado a un id de nodo.
     */
    @Override
    public BloqueHorario obtenerBloque(String id) {
        return nodos.get(id);
    }

    /**
     * Numero total de nodos.
     */
    @Override
    public int obtenerNumeroNodos() {
        return nodos.size();
    }

    /**
     * Numero total de aristas.
     */
    @Override
    public int obtenerNumeroAristas() {
        return numAristas;
    }

    /**
     * Lista de todos los bloques almacenados como nodos.
     */
    public List<BloqueHorario> obtenerTodosLosBloques() {
        return new ArrayList<>(nodos.values());
    }

    /**
     * Calcula estadisticas basicas (grado promedio y densidad).
     */
    public Map<String, Double> obtenerEstadisticas() {
        Map<String, Double> stats = new HashMap<>();

        double gradoTotal = 0;
        for (Set<String> vecinos : adyacencias.values()) {
            gradoTotal += vecinos.size();
        }
        double gradoPromedio = nodos.isEmpty() ? 0 : gradoTotal / nodos.size();

        int n = nodos.size();
        double maxAristas = n * (n - 1) / 2.0;
        double densidad = maxAristas == 0 ? 0 : numAristas / maxAristas;

        stats.put("grado_promedio", gradoPromedio);
        stats.put("densidad", densidad);

        return stats;
    }

    /**
     * Devuelve el catalogo de recursos.
     */
    public CatalogoRecursos getCatalogo() {
        return catalogo;
    }

    /**
     * Devuelve el validador central usado para evaluar conflictos.
     */
    public ValidadorDeHorarios getValidador() {
        return validador;
    }

    /**
     * Aplica una coloracion greedy (DSatur simplificado) y asigna un color por nodo.
     */
    public Map<String, Integer> colorear() {
        Map<String, Integer> colores = new HashMap<>();
        List<String> ids = new ArrayList<>(nodos.keySet());

        if (ids.isEmpty()) {
            return colores;
        }

        ids.sort((a, b) -> Integer.compare(
                adyacencias.get(b).size(),
                adyacencias.get(a).size()
        ));

        colores.put(ids.get(0), 0);

        for (int i = 1; i < ids.size(); i++) {
            String idActual = ids.get(i);
            Set<String> vecinos = adyacencias.get(idActual);

            Set<Integer> coloresUsados = new HashSet<>();
            for (String vecino : vecinos) {
                if (colores.containsKey(vecino)) {
                    coloresUsados.add(colores.get(vecino));
                }
            }

            int colorAsignado = 0;
            while (coloresUsados.contains(colorAsignado)) {
                colorAsignado++;
            }

            colores.put(idActual, colorAsignado);
        }

        return colores;
    }

    /**
     * Aplica coloracion usando el algoritmo DSatur, devolviendo color por id de bloque.
     */
    public Map<String, Integer> colorearConDSatur() {
        Map<String, Integer> colores = new HashMap<>();
        if (nodos.isEmpty()) {
            return colores;
        }

        Map<String, Integer> saturacion = new HashMap<>();
        Map<String, Integer> grado = new HashMap<>();
        List<String> nodosNoColoreados = new ArrayList<>(nodos.keySet());

        for (String id : nodosNoColoreados) {
            saturacion.put(id, 0);
            grado.put(id, adyacencias.get(id).size());
        }

        while (colores.size() < nodos.size()) {
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

            if (nodoAColorear == null) {
                break;
            }

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

            colores.put(nodoAColorear, colorAsignado);
            nodosNoColoreados.remove(nodoAColorear);

            for (String vecinoId : adyacencias.get(nodoAColorear)) {
                if (!colores.containsKey(vecinoId)) {
                    Set<Integer> coloresVecino = new HashSet<>();
                    for (String v : adyacencias.get(vecinoId)) {
                        if (colores.containsKey(v)) {
                            coloresVecino.add(colores.get(v));
                        }
                    }
                    saturacion.put(vecinoId, coloresVecino.size());
                }
            }
        }

        return colores;
    }
}
