/*
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EstrategiaColoracion implements EstrategiaGeneracion {
    @Override
    public HorarioSemana generarHorario(AdaptadorGraficaDeHorarios horarioGrafica) {
          Obtener adyacencias como Sets (ahora directamente sin conversión)
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

        // Definir horas permitidas para las clases
        List<Integer> horasDisponibles = Arrays.asList(7, 8, 9, 10, 11, 12, 13, 14);

        // Control de índice por día
        Map<String, Integer> indiceHoraPorDia = new HashMap<>();
        for (String dia : dias)
            indiceHoraPorDia.put(dia, 0);

        // Agregar los bloques al horario
        for (String bloqueID : adyacencias.keySet()) {
            BloqueHorario bloque = horarioGrafica.obtenerBloque(bloqueID);
            if (bloque != null) {
                horarioSemana.agregar(bloque);
            }
        }

        // Asignación final (día basado en color)
        for (Map.Entry<String, String> entry : asignacion.entrySet()) {
            String bloqueID = entry.getKey();
            String dia = entry.getValue();

            BloqueHorario bloque = horarioGrafica.obtenerBloque(bloqueID);

            int idx = indiceHoraPorDia.get(dia);

            // Si se acabaron las horas disponibles, mover al siguiente día
            if (idx >= horasDisponibles.size()) {
                int currentIndex = dias.indexOf(dia);
                int nextIndex = (currentIndex + 1) % dias.size();
                dia = dias.get(nextIndex);
                idx = 0;
                indiceHoraPorDia.put(dia, 1);
            }

            int horaInicio = horasDisponibles.get(idx);
            int horaFin = horaInicio + bloque.getDuracionHoras();

            bloque.setHoraInicio(horaInicio);
            bloque.setHoraFin(horaFin);

            horarioSemana.asignarBloqueADia(bloqueID, dia);

            indiceHoraPorDia.put(dia, idx + 1);
        }

        return horarioSemana;

    }
}
*/