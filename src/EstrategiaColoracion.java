package src;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia de generación de horarios usando k-coloración de grafos.
 * CORREGIDO: Ahora respeta la disponibilidad de profesores al asignar días.
 * 
 * Proceso en 3 fases:
 * 1. Construcción de gráfica de conflictos
 * 2. Coloración del grafo con validación de disponibilidad (asignación de días)
 * 3. Asignación de horas dentro de cada día
 */
public class EstrategiaColoracion implements EstrategiaGeneracion {
    
    @Override
    public HorarioSemana generarHorario(AdaptadorGraficaDeHorarios horarioGrafica) {
        
        // FASE 1: Construir gráfica de conflictos
        System.out.println("\n=== FASE 1: Construcción de gráfica ===");
        horarioGrafica.construirGraficaAutomaticamente();
        System.out.println("Gráfica construida con " + horarioGrafica.obtenerNumeroNodos() + 
                           " nodos y " + horarioGrafica.obtenerNumeroAristas() + " aristas");

        // FASE 2: Colorear grafo con DSatur RESPETANDO disponibilidad de profesores
        System.out.println("\n=== FASE 2: Coloración con DSatur (asignación de días) ===");
        
        List<String> dias = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
        Map<String, Integer> colores = colorearConDisponibilidad(horarioGrafica, dias);
        
        // Determinar cuántos colores (días) se necesitaron
        int maxColor = colores.values().stream().max(Integer::compare).orElse(-1);
        int numColoresUsados = maxColor + 1;
        System.out.println("Se necesitaron " + numColoresUsados + " colores (días)");
        
        if (numColoresUsados > dias.size()) {
            System.out.println("ADVERTENCIA: Se necesitan " + numColoresUsados + 
                             " días pero solo hay " + dias.size() + " disponibles");
        }
        
        // Crear HorarioSemana
        HorarioSemana horarioSemana = new HorarioSemana();
        for (String dia : dias) {
            horarioSemana.agregarDia(new HorarioDia(dia));
        }
        
        // Asignar bloques a días según el color
        for (Map.Entry<String, Integer> entry : colores.entrySet()) {
            String bloqueId = entry.getKey();
            int color = entry.getValue();
            BloqueHorario bloque = horarioGrafica.obtenerBloque(bloqueId);
            
            if (color < dias.size()) {
                String dia = dias.get(color);
                bloque.setDia(dia);
                horarioSemana.agregarBloqueEnDia(dia, bloque);
                System.out.println("  Bloque " + bloque.getMateria() + " -> " + dia);
            } else {
                System.out.println("  Bloque " + bloque.getMateria() + " -> SIN ASIGNAR (color " + color + ")");
                horarioSemana.agregarBloqueSinAsignar(bloque);
            }
        }
        
        // FASE 3: Asignar horas dentro de cada día
        System.out.println("\n=== FASE 3: Asignación de horas ===");
        List<Validador> validadores = Arrays.asList(
            new ValidadorPorProfesor(),
            new ValidadorPorSalon(),
            new ValidadorPorHora()
        );
        
        AsignadorHorasLocalTime asignadorHoras = new AsignadorHorasLocalTime(
            horarioGrafica.getCatalogo(), 
            LocalTime.of(7, 0),
            LocalTime.of(15, 0), // CORREGIDO: Horario más realista (7am-3pm)
            validadores
        );
        
        asignadorHoras.asignarHoras(horarioSemana);
        System.out.println("Horas asignadas exitosamente");
        
        return horarioSemana;
    }
    
    /**
     * NUEVO: Coloración que respeta la disponibilidad de profesores.
     * Similar a DSatur pero con restricciones adicionales.
     */
    private Map<String, Integer> colorearConDisponibilidad(
            AdaptadorGraficaDeHorarios grafica, 
            List<String> dias) {
        
        Map<String, Integer> colores = new HashMap<>();
        Map<String, Set<String>> adyacencias = grafica.obtenerAdyacencias();
        List<String> nodos = new ArrayList<>(adyacencias.keySet());
        
        if (nodos.isEmpty()) {
            return colores;
        }
        
        // Saturación: colores distintos usados por vecinos
        Map<String, Set<Integer>> saturacion = new HashMap<>();
        for (String id : nodos) {
            saturacion.put(id, new HashSet<>());
        }
        
        // 1. Colorear el nodo con mayor grado primero
        String primero = nodos.stream()
                .max(Comparator.comparingInt(id -> adyacencias.get(id).size()))
                .orElse(nodos.get(0));
        
        int colorPrimero = asignarMejorColor(primero, grafica, colores, dias, saturacion.get(primero));
        colores.put(primero, colorPrimero);
        
        // Actualizar saturación de vecinos
        for (String vecino : adyacencias.get(primero)) {
            saturacion.get(vecino).add(colorPrimero);
        }
        
        // 2. Colorear el resto por saturación (DSatur)
        while (colores.size() < nodos.size()) {
            
            // Encontrar nodo con máxima saturación (y mayor grado en empates)
            String siguiente = nodos.stream()
                    .filter(id -> !colores.containsKey(id))
                    .max(Comparator
                            .comparingInt((String id) -> saturacion.get(id).size())
                            .thenComparingInt(id -> adyacencias.get(id).size()))
                    .orElse(null);
            
            if (siguiente == null) break;
            
            // Asignar el mejor color disponible respetando disponibilidad
            int colorAsignado = asignarMejorColor(
                    siguiente, grafica, colores, dias, saturacion.get(siguiente));
            
            colores.put(siguiente, colorAsignado);
            
            // Actualizar saturación de vecinos no coloreados
            for (String vecino : adyacencias.get(siguiente)) {
                if (!colores.containsKey(vecino)) {
                    saturacion.get(vecino).add(colorAsignado);
                }
            }
        }
        
        return colores;
    }
    
    /**
     * NUEVO: Encuentra el mejor color (día) para un bloque respetando:
     * 1. Disponibilidad del profesor
     * 2. No conflicto con vecinos ya coloreados
     */
    private int asignarMejorColor(
            String bloqueId,
            AdaptadorGraficaDeHorarios grafica,
            Map<String, Integer> coloresAsignados,
            List<String> dias,
            Set<Integer> coloresVecinos) {
        
        BloqueHorario bloque = grafica.obtenerBloque(bloqueId);
        CatalogoRecursos catalogo = grafica.getCatalogo();
        
        // Obtener días disponibles del profesor
        Set<String> diasDisponiblesProfesor = getDiasDisponiblesProfesor(bloque, catalogo);
        
        // Intentar asignar color respetando disponibilidad
        for (int color = 0; color < dias.size() * 2; color++) { // *2 para permitir extensión
            
            // Si el color está usado por un vecino, saltar
            if (coloresVecinos.contains(color)) {
                continue;
            }
            
            // Si el color está dentro del rango de días
            if (color < dias.size()) {
                String dia = dias.get(color);
                
                if (!esDiaValidoParaBloque(bloque, dia, coloresVecinos, diasDisponiblesProfesor)) {
                    continue;
                }
            }
            
            // Este color es válido
            return color;
        }
        
        // Si no se encontró color válido, devolver el mínimo disponible
        int color = 0;
        while (coloresVecinos.contains(color)) {
            color++;
        }
        System.out.println("  ADVERTENCIA: No se encontró día disponible para " + 
                         bloque.getMateria() + ", asignando color " + color);
        return color;
    }

    private boolean esDiaValidoParaBloque(BloqueHorario bloque, String dia, Set<Integer> coloresVecinos, Set<String> diasDisponiblesProfesor) {
        int color = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes").indexOf(dia);
        if (coloresVecinos.contains(color)) {
            return false;
        }
        return diasDisponiblesProfesor.isEmpty() || diasDisponiblesProfesor.contains(dia);
    }


    
    private Set<String> getDiasDisponiblesProfesor(BloqueHorario bloque, CatalogoRecursos catalogo) {
        if (bloque.getProfesorId() == null) {
            return Collections.emptySet(); // Sin restricción
        }
        
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return Collections.emptySet(); // Sin restricción
        }
        
        List<String> dias = profesor.getDiasDisponibles();
        if (dias == null || dias.isEmpty()) {
            return Collections.emptySet(); // Sin restricción
        }
        
        return new HashSet<>(dias);
    }
    
    private boolean hayConflictoEnDia(
            BloqueHorario bloque,
            int color,
            AdaptadorGraficaDeHorarios grafica,
            Map<String, Integer> coloresAsignados,
            ValidadorDeHorarios validador) {
        
        // Obtener todos los bloques ya asignados a este color
        List<BloqueHorario> bloquesEnMismoDia = coloresAsignados.entrySet().stream()
                .filter(e -> e.getValue().equals(color))
                .map(e -> grafica.obtenerBloque(e.getKey()))
                .collect(Collectors.toList());
        
        // Verificar conflictos usando el validador centralizado
        for (BloqueHorario otro : bloquesEnMismoDia) {
            if (validador.hayConflictoDirecto(bloque, otro)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Descontinuado: Ya no se usa, se mantiene por compatibilidad
     */
    @Deprecated
    private boolean esProfesorDisponible(BloqueHorario bloque, String dia, CatalogoRecursos catalogo) {
        if (bloque.getProfesorId() == null) {
            return true;
        }
        
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return true;
        }
        
        List<String> diasDisponibles = profesor.getDiasDisponibles();
        if (diasDisponibles == null || diasDisponibles.isEmpty()) {
            return true;
        }
        
        return diasDisponibles.stream().anyMatch(d -> d.equalsIgnoreCase(dia));
    }
    
    /**
     * Descontinuado: Ya no se usa, se mantiene por compatibilidad
     */
    @Deprecated
    private String encontrarDiaDisponible(BloqueHorario bloque, List<String> dias, CatalogoRecursos catalogo) {
        for (String dia : dias) {
            if (esProfesorDisponible(bloque, dia, catalogo)) {
                return dia;
            }
        }
        return null;
    }
}