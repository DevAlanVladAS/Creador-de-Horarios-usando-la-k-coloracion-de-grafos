package src;
import java.util.*;

public class AsignadorDeDias {

    private final transient List<Validador> validadores;

    public AsignadorDeDias(List<Validador> validadores) {
        this.validadores = validadores != null ? validadores : new ArrayList<>();
    }

    public HorarioSemana asignarDias(List<BloqueHorario> nodos,
            int[] colores,
            List<String> dias) {

        HorarioSemana semana = new HorarioSemana();
        for (String d : dias)
            semana.agregarDia(new HorarioDia(d));

        for (int i = 0; i < nodos.size(); i++) {
            BloqueHorario b = nodos.get(i);
            int color = colores[i];

            if (color >= dias.size()) {
                // más colores que días: bloque sin asignar
                semana.agregar(b);
                continue;
            }

            String dia = dias.get(color);

            b.setDia(dia);
            semana.agregarBloqueEnDia(dia, b);
        }

        return semana;
    }

    private boolean esValidoEnDia(BloqueHorario b, String dia, HorarioSemana semana) {
        List<BloqueHorario> delDia = semana.getDiasSemana().stream()
                .filter(d -> d.getDia().equalsIgnoreCase(dia))
                .findFirst()
                .map(HorarioDia::getBloques)
                .orElse(Collections.emptyList());
        for (BloqueHorario otro : delDia) {
             for (Validador v : validadores) {
                List<ResultadoValidacion> resultados = v.validar(b, otro, semana);
                for (ResultadoValidacion res : resultados) {
                    if (res.getSeveridad() == ResultadoValidacion.Severidad.ERROR) {
                        return false; // Conflicto duro encontrado
                    }
                }
            }
        }
        return true;
    }
}
