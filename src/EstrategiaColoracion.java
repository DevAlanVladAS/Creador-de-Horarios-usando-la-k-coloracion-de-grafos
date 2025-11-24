package src;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia de generacion de horarios via k-coloracion:
 * construye la grafica de conflictos, colorea respetando disponibilidad,
 * y luego asigna horas dentro de cada dia.
 */
public class EstrategiaColoracion implements EstrategiaGeneracion {
    
    @Override
    public HorarioSemana generarHorario(AdaptadorGraficaDeHorarios horarioGrafica) {
        
        // FASE 1: Construir grafica de conflictos
        System.out.println("\n=== FASE 1: Construccion de grafica ===");
        horarioGrafica.construirGraficaAutomaticamente();
        System.out.println("Grafica construida con " + horarioGrafica.obtenerNumeroNodos() + 
                           " nodos y " + horarioGrafica.obtenerNumeroAristas() + " aristas");

        // FASE 2: Colorear grafo respetando disponibilidad de profesores
        System.out.println("\n=== FASE 2: Coloracion con DSatur (asignacion de dias) ===");
        
        List<String> dias = Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes");
        Map<String, Integer> colores = colorearConDisponibilidad(horarioGrafica, dias);
        
        int maxColor = colores.values().stream().max(Integer::compare).orElse(-1);
        int numColoresUsados = maxColor + 1;
        System.out.println("Se necesitaron " + numColoresUsados + " colores (dias)");
        
        if (numColoresUsados > dias.size()) {
            System.out.println("ADVERTENCIA: Se necesitan " + numColoresUsados + 
                             " dias pero solo hay " + dias.size() + " disponibles");
        }
        
        HorarioSemana horarioSemana = new HorarioSemana();
        for (String dia : dias) {
            horarioSemana.agregarDia(new HorarioDia(dia));
        }
        
        // Asignar bloques a dias segun color
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
        
        // FASE 3: Asignar horas dentro de cada dia
        System.out.println("\n=== FASE 3: Asignacion de horas ===");
        List<Validador> validadores = Arrays.asList(
            new ValidadorPorProfesor(),
            new ValidadorPorSalon(),
            new ValidadorPorHora()
        );
        
        AsignadorHorasLocalTime asignadorHoras = new AsignadorHorasLocalTime(
            horarioGrafica.getCatalogo(), 
            LocalTime.of(7, 0),
            LocalTime.of(15, 0),
            validadores
        );
        
        asignadorHoras.asignarHoras(horarioSemana);
        System.out.println("Horas asignadas exitosamente");
        
        return horarioSemana;
    }
    
    /**
     * Colorea la grafica respetando disponibilidad de profesor y conflictos.
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
        
        Map<String, Set<Integer>> saturacion = new HashMap<>();
        for (String id : nodos) {
            saturacion.put(id, new HashSet<>());
        }
        
        String primero = nodos.stream()
                .max(Comparator.comparingInt(id -> adyacencias.get(id).size()))
                .orElse(nodos.get(0));
        
        int colorPrimero = asignarMejorColor(primero, grafica, colores, dias, saturacion.get(primero));
        colores.put(primero, colorPrimero);
        
        for (String vecino : adyacencias.get(primero)) {
            saturacion.get(vecino).add(colorPrimero);
        }
        
        while (colores.size() < nodos.size()) {
            
            String siguiente = nodos.stream()
                    .filter(id -> !colores.containsKey(id))
                    .max(Comparator
                            .comparingInt((String id) -> saturacion.get(id).size())
                            .thenComparingInt(id -> adyacencias.get(id).size()))
                    .orElse(null);
            
            if (siguiente == null) break;
            
            int colorAsignado = asignarMejorColor(
                    siguiente, grafica, colores, dias, saturacion.get(siguiente));
            
            colores.put(siguiente, colorAsignado);
            
            for (String vecino : adyacencias.get(siguiente)) {
                if (!colores.containsKey(vecino)) {
                    saturacion.get(vecino).add(colorAsignado);
                }
            }
        }
        
        return colores;
    }
    
    /**
     * Selecciona el mejor color (dia) posible para un bloque respetando disponibilidad.
     */
    private int asignarMejorColor(
            String bloqueId,
            AdaptadorGraficaDeHorarios grafica,
            Map<String, Integer> coloresAsignados,
            List<String> dias,
            Set<Integer> coloresVecinos) {
        
        BloqueHorario bloque = grafica.obtenerBloque(bloqueId);
        CatalogoRecursos catalogo = grafica.getCatalogo();
        
        Set<String> diasDisponiblesProfesor = getDiasDisponiblesProfesor(bloque, catalogo);
        
        for (int color = 0; color < dias.size() * 2; color++) {
            
            if (coloresVecinos.contains(color)) {
                continue;
            }
            
            if (color < dias.size()) {
                String dia = dias.get(color);
                
                if (!esDiaValidoParaBloque(bloque, dia, coloresVecinos, diasDisponiblesProfesor)) {
                    continue;
                }
            }
            
            return color;
        }
        
        int color = 0;
        while (coloresVecinos.contains(color)) {
            color++;
        }
        System.out.println("  ADVERTENCIA: No se encontro dia disponible para " + 
                         bloque.getMateria() + ", asignando color " + color);
        return color;
    }

    /**
     * Valida si un dia es aceptable para un bloque (disponibilidad y conflictos previos).
     */
    private boolean esDiaValidoParaBloque(BloqueHorario bloque, String dia, Set<Integer> coloresVecinos, Set<String> diasDisponiblesProfesor) {
        int color = Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes").indexOf(dia);
        if (coloresVecinos.contains(color)) {
            return false;
        }
        return diasDisponiblesProfesor.isEmpty() || diasDisponiblesProfesor.contains(dia);
    }

    /**
     * Obtiene los dias disponibles del profesor del bloque (vacio si no hay restricciones).
     */
    private Set<String> getDiasDisponiblesProfesor(BloqueHorario bloque, CatalogoRecursos catalogo) {
        if (bloque.getProfesorId() == null) {
            return Collections.emptySet();
        }
        
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return Collections.emptySet();
        }
        
        List<String> dias = profesor.getDiasDisponibles();
        if (dias == null || dias.isEmpty()) {
            return Collections.emptySet();
        }
        
        return new HashSet<>(dias);
    }
    
    /**
     * Deprecated: logica antigua de disponibilidad (mantenido por compatibilidad).
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
     * Deprecated: logica antigua para buscar el primer dia disponible.
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
