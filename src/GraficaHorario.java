package src;
import java.util.Map;
import java.util.Set;

/**
 * Interfaz que define operaciones básicas para una gráfica de conflictos de horarios.
 * Nodos representan bloques (por id), aristas representan conflictos.
 */
public interface GraficaHorario {
    /**
     * Agrega un nodo a la grafica.
     * @param bloqueId ID unico del bloque
     * @param bloque referencia al objeto BloqueHorario para consultar datos
     */
    void agregarNodo(String bloqueId, BloqueHorario bloque);

    /**
     * Agrega una arista (conflicto) entre dos nodos.
     * @param bloqueIdA ID del bloque A
     * @param bloqueIdB ID del bloque B
     */
    void agregarArista(String bloqueIdA, String bloqueIdB);

    /**
     * Muestra la gráfica en consola (útil para debugging).
     */
    void mostrar();

    /**
     * Obtiene el mapa de adyacencias (id -> set de ids conflictivos).
     * Util para algoritmos de coloracion y backtracking.
     */
    Map<String, Set<String>> obtenerAdyacencias();

    /**
     * Obtiene un bloque por su id.
     */
    BloqueHorario obtenerBloque(String id);

    /**
     * Obtiene el numero total de nodos.
     */
    int obtenerNumeroNodos();

    /**
     * Obtiene el numero total de aristas.
     */
    int obtenerNumeroAristas();
}
