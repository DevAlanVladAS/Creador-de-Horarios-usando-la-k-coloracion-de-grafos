package src;

import java.util.Map;
import java.util.Set;

/**
 * Interfaz para la grafica de conflictos de horarios:
 * nodos son bloques y aristas representan incompatibilidades.
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
     * Muestra la grafica en consola (util para debugging).
     */
    void mostrar();

    /**
     * Mapa de adyacencias (id -> set de ids conflictivos).
     */
    Map<String, Set<String>> obtenerAdyacencias();

    /**
     * Obtiene un bloque por su id.
     */
    BloqueHorario obtenerBloque(String id);

    /**
     * Numero total de nodos.
     */
    int obtenerNumeroNodos();

    /**
     * Numero total de aristas.
     */
    int obtenerNumeroAristas();
}
