
package src;

import java.time.LocalTime;
import java.util.*;

/**
 * Estrategia de generación de horarios usando k-coloración de grafos.
 * 
 * Proceso en 3 fases:
 * 1. Construcción de gráfica de conflictos
 * 2. Coloración del grafo (asignación de días)
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
        
        // FASE 2: Colorear grafo (asignar días)
        System.out.println("\n=== FASE 2: Coloración (asignación de días) ===");
        Map<String, Integer> colores = horarioGrafica.colorear();
        
        // Determinar cuántos colores (días) se necesitaron
        int maxColor = colores.values().stream().max(Integer::compare).orElse(-1);
        int numColoresUsados = maxColor + 1;
        System.out.println("Se necesitaron " + numColoresUsados + " colores (días)");
        
        // Crear días de la semana
        List<String> dias = Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes");
        
        if (numColoresUsados > dias.size()) {
            System.out.println("ADVERTENCIA: Se necesitan más días de los disponibles");
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
                
                // Verificar disponibilidad del profesor
                if (esProfesorDisponible(bloque, dia, horarioGrafica.getCatalogo())) {
                    bloque.setDia(dia);
                    horarioSemana.agregarBloqueEnDia(dia, bloque);
                    System.out.println("  " + bloque.getMateria() + " -> " + dia);
                } else {
                    // Intentar con otro día disponible
                    String diaAlternativo = encontrarDiaDisponible(bloque, dias, horarioGrafica.getCatalogo());
                    if (diaAlternativo != null) {
                        bloque.setDia(diaAlternativo);
                        horarioSemana.agregarBloqueEnDia(diaAlternativo, bloque);
                        System.out.println("  " + bloque.getMateria() + " -> " + diaAlternativo + " (alternativo)");
                    } else {
                        horarioSemana.agregarBloqueSinAsignar(bloque);
                        System.out.println("  " + bloque.getMateria() + " -> SIN ASIGNAR (profesor no disponible)");
                    }
                }
            } else {
                horarioSemana.agregarBloqueSinAsignar(bloque);
            }
        }
        
        // FASE 3: Asignar horas dentro de cada día
        System.out.println("\n=== FASE 3: Asignación de horas ===");
        List<Validador> validadores = Arrays.asList(
            new ValidadorPorProfesor(horarioGrafica.getCatalogo()),
            new ValidadorPorSalon(horarioGrafica.getCatalogo()),
            new ValidadorPorHora()
        );
        
        AsignadorHorasLocalTime asignadorHoras = new AsignadorHorasLocalTime(
            LocalTime.of(7, 0),
            LocalTime.of(20, 0),
            validadores
        );
        
        asignadorHoras.asignarHoras(horarioSemana);
        System.out.println("Horas asignadas exitosamente");
        
        return horarioSemana;
    }
    
    private boolean esProfesorDisponible(BloqueHorario bloque, String dia, CatalogoRecursos catalogo) {
        if (bloque.getProfesorId() == null) {
            return true;
        }
        
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return true;
        }
        
        return profesor.getDiasDisponibles().contains(dia);
    }
    
    private String encontrarDiaDisponible(BloqueHorario bloque, List<String> dias, CatalogoRecursos catalogo) {
        for (String dia : dias) {
            if (esProfesorDisponible(bloque, dia, catalogo)) {
                return dia;
            }
        }
        return null;
    }
}