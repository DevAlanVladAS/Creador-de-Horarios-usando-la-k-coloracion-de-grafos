import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EstrategiaColoracion implements EstrategiaGeneracion {
    @Override
    public HorarioSemana generarHorario(AdaptadorGraficaDeHorarios horarioGrafica) {
        // Obtener adyacencias como Sets (ahora directamente sin conversión)
        Map<String, Set<String>> adyacencias = horarioGrafica.obtenerAdyacencias();
        
        List<String> dias = Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes");
        Map<String, String> asignacion = new HashMap<>();

        for (String bloqueID : adyacencias.keySet()) {
            Set<String> diasUsados = new java.util.HashSet<>();
            for (String ady : adyacencias.get(bloqueID)) {
                if (asignacion.containsKey(ady)) {
                    diasUsados.add(asignacion.get(ady));
                }
            }
            // Buscar el primer día disponible
            for (String dia : dias) {
                if (!diasUsados.contains(dia)) {
                    asignacion.put(bloqueID, dia);
                    break;
                }
            }
        }

        // Crear un nuevo HorarioSemana y agregar los días
        HorarioSemana horarioSemana = new HorarioSemana();
        for (String dia : dias) {
            horarioSemana.agregarDia(new HorarioDia(dia));
        }
        
        // Agregar los bloques sin asignar primero
        for (String bloqueID : adyacencias.keySet()) {
            BloqueHorario bloque = horarioGrafica.obtenerBloque(bloqueID);
            if (bloque != null) {
                horarioSemana.agregar(bloque);
            }
        }
        
        // Asignar los bloques a los días
        for (Map.Entry<String, String> entry : asignacion.entrySet()) {
            String bloqueID = entry.getKey();
            String dia = entry.getValue();
            horarioSemana.assignBlockToDay(bloqueID, dia);
        }
        return horarioSemana;
    }
}
