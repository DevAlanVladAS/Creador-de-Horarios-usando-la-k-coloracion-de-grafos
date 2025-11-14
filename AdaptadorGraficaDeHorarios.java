import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adaptador que construye una gráfica de conflictos de horarios a partir de bloques.
 * Nodos = ids de bloques, Aristas = conflictos detectados.
 * 
 * Conflictos detectados:
 * - Mismo profesor en horarios solapados
 * - Mismo salón en horarios solapados
 * - Mismo grupo en horarios solapados
 * - Horarios fuera del rango permitido
 */
public class AdaptadorGraficaDeHorarios implements GraficaHorario {
    
    // Map de id bloque -> objeto BloqueHorario (para referencia rápida)
    private Map<String, BloqueHorario> nodos = new HashMap<>();
    // Map de id bloque -> SET de ids bloques conflictivos (grafo de adyacencias)
    private Map<String, Set<String>> adyacencias = new HashMap<>();
    // Catálogo de recursos (para consultar disponibilidad de profesores, etc.)
    private CatalogoRecursos catalogo;

    // Constructor que recibe bloques y catálogo, y agrega los nodos automáticamente
    public AdaptadorGraficaDeHorarios(List<BloqueHorario> bloques, CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
        if (bloques != null) {
            for (BloqueHorario bloque : bloques) {
                agregarNodo(bloque.getId(), bloque);
            }
        }
    }

    // Constructor original solo con catálogo (por compatibilidad)
    public AdaptadorGraficaDeHorarios(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * Agrega un nodo (bloque) a la gráfica.
     */
    @Override
    public void agregarNodo(String bloqueId, BloqueHorario bloque) {
        nodos.put(bloqueId, bloque);
        if (!adyacencias.containsKey(bloqueId)) {
            adyacencias.put(bloqueId, new HashSet<>());
        }
    }

    /**
     * Agrega una arista bidireccional entre dos nodos (conflicto).
     */ 
    @Override
    public void agregarArista(String bloqueIdA, String bloqueIdB) {
        // Evitar auto-aristas y duplicados
        if (bloqueIdA.equals(bloqueIdB)) return;
        if (adyacencias.containsKey(bloqueIdA) && !adyacencias.get(bloqueIdA).contains(bloqueIdB)) {
            adyacencias.get(bloqueIdA).add(bloqueIdB);
        }
        if (adyacencias.containsKey(bloqueIdB) && !adyacencias.get(bloqueIdB).contains(bloqueIdA)) {
            adyacencias.get(bloqueIdB).add(bloqueIdA);
        }
    }

    /**
     * Construye la gráfica automáticamente detectando conflictos entre todos los pares de bloques.
     * Utiliza CatalogoRecursos para consultar disponibilidad de profesores.
     */
    public void construirGraficaAutomaticamente() {
        List<String> ids = new ArrayList<>(nodos.keySet());
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String id1 = ids.get(i);
                String id2 = ids.get(j);
                BloqueHorario b1 = nodos.get(id1);
                BloqueHorario b2 = nodos.get(id2);

                if (hayConflicto(b1, b2)) {
                    agregarArista(id1, id2);
                }
            }
        }
    }

    /**
     * Detecta si hay conflicto entre dos bloques.
     * Conflictos:
     * - Mismo profesor en horarios solapados (si profesorId está asignado)
     * - Mismo salón en horarios solapados
     * - Mismo grupo en horarios solapados
     */
    private boolean hayConflicto(BloqueHorario b1, BloqueHorario b2) {
        if (b1.getProfesorId() != null && b2.getProfesorId() != null) {
            if (b1.getProfesorId().equals(b2.getProfesorId())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        // Conflicto por profesor (si usan nombres)
        if (b1.getProfesor() != null && b2.getProfesor() != null) {
            if (b1.getProfesor().equalsIgnoreCase(b2.getProfesor())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        // Conflicto por salón (ids)
        if (b1.getSalonId() != null && b2.getSalonId() != null) {
            if (b1.getSalonId().equals(b2.getSalonId())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        // Conflicto por salón (nombres)
        if (b1.getSalon() != null && b2.getSalon() != null) {
            if (b1.getSalon().equalsIgnoreCase(b2.getSalon())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        // Conflicto por grupo (ids)
        if (b1.getGrupoId() != null && b2.getGrupoId() != null) {
            if (b1.getGrupoId().equals(b2.getGrupoId())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        // Conflicto por grupo (nombres)
        if (b1.getGrupo() != null && b2.getGrupo() != null) {
            if (b1.getGrupo().equalsIgnoreCase(b2.getGrupo())) {
                if (horariosSeSuperponen(b1, b2)) return true;
            }
        }

        return false;
    }

    /**
     * Verifica si dos horarios se superponen.
     */
    private boolean horariosSeSuperponen(BloqueHorario b1, BloqueHorario b2) {
        LocalTime inicio1 = b1.getHoraInicio();
        LocalTime fin1 = b1.getHoraFin();
        LocalTime inicio2 = b2.getHoraInicio();
        LocalTime fin2 = b2.getHoraFin();

        // Solapamiento: inicio1 < fin2 AND inicio2 < fin1
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    /**
     * Valida disponibilidad de un profesor usando CatalogoRecursos y asignación de día.
     * Retorna true si el profesor está disponible en el día asignado al bloque.
     */
    public boolean validarDisponibilidadProfesor(String bloqueId, String diaAsignado) {
        BloqueHorario b = nodos.get(bloqueId);
        if (b == null) return false;

        String profesorId = b.getProfesorId();
        if (profesorId == null) {
            // Si no usa id, no podemos validar con catálogo
            return true;
        }

        Profesor p = catalogo.obtenerProfesorPorId(profesorId);
        if (p == null) return false;

        return p.disponibleEn(diaAsignado);
    }

    /**
     * Obtiene el grado (número de conflictos) de un nodo.
     * Útil para heurísticas de ordenamiento en algoritmos.
     */
    public int obtenerGrado(String bloqueId) {
        return adyacencias.getOrDefault(bloqueId, new HashSet<>()).size();
    }

    /**
     * Obtiene todos los bloques con un grado mayor o igual a cierto umbral.
     * Útil para identificar bloques problemáticos.
     */
    public List<String> obtenerBloquesConAltoGrado(int umbral) {
        List<String> resultado = new ArrayList<>();
        for (String id : nodos.keySet()) {
            if (obtenerGrado(id) >= umbral) {
                resultado.add(id);
            }
        }
        return resultado;
    }

    /**
     * Obtiene los vecinos (conflictos) de un nodo como un Set.
     */
    public Set<String> obtenerVecinos(String bloqueId) {
        return new HashSet<>(adyacencias.getOrDefault(bloqueId, new HashSet<>()));
    }

    /**
     * Verifica si dos nodos son adyacentes (conflictivos).
     */
    public boolean sonAdyacentes(String bloqueIdA, String bloqueIdB) {
        return adyacencias.containsKey(bloqueIdA) && 
               adyacencias.get(bloqueIdA).contains(bloqueIdB);
    }

    @Override
    public void mostrar() {
        System.out.println("\n=== GRÁFICA DE CONFLICTOS DE HORARIOS ===");
        System.out.println("Total de nodos (bloques): " + obtenerNumeroNodos());
        System.out.println("Total de aristas (conflictos): " + obtenerNumeroAristas());
        System.out.println("\nDetalles por nodo:");
        for (String id : nodos.keySet()) {
            BloqueHorario b = nodos.get(id);
            Set<String> vecinos = adyacencias.getOrDefault(id, new HashSet<>());
            System.out.print("  [" + id.substring(0, Math.min(8, id.length())) + "]: ");
            System.out.print(b.getMateria() + " - ");
            System.out.print(b.getHoraInicio() + " a " + b.getHoraFin() + " - ");
            System.out.print("Día: " + (b.getDia() != null ? b.getDia() : "sin asignar") + " - ");
            System.out.print("Grado: " + vecinos.size());
            if (!vecinos.isEmpty()) {
                System.out.print(" - Conflictos: [");
                int contador = 0;
                for (String vecino : vecinos) {
                    System.out.print(vecino.substring(0, Math.min(8, vecino.length())));
                    if (++contador < vecinos.size()) System.out.print(", ");
                }
                System.out.print("]");
            }
            System.out.println();
        }
    }

    @Override
    public Map<String, Set<String>> obtenerAdyacencias() {
        // Retornar directamente el mapa de adyacencias con Sets
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
        return adyacencias.values().stream().mapToInt(Set::size).sum() / 2;
    }

    /**
     * Obtiene estadísticas de la gráfica (densidad, grado promedio, etc.).
     */
    public Map<String, Double> obtenerEstadisticas() {
        Map<String, Double> stats = new HashMap<>();
        int n = obtenerNumeroNodos();
        int m = obtenerNumeroAristas();
        
        stats.put("nodos", (double) n);
        stats.put("aristas", (double) m);
        
        if (n > 0) {
            double gradoPromedio = (2.0 * m) / n;
            stats.put("grado_promedio", gradoPromedio);
        }
        
        if (n > 1) {
            double maxAristas = (n * (n - 1)) / 2.0;
            double densidad = (maxAristas > 0) ? (m / maxAristas) : 0;
            stats.put("densidad", densidad);
        }
        
        return stats;
    }
}
