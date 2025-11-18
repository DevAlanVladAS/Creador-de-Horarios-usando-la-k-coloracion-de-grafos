package src;
import java.time.*;
import java.util.*;

/**
 * Fase 3: Asignación de horas dentro de un día usando LocalTime.
 * - Ordena bloques por duración (más largos primero).
 * - Intenta colocarlos empezando desde horaInicioDia en intervalos de 30 min.
 * - Respeta validadores de conflicto (recurso, profesor, grupo, etc.)
 * - Actualiza horaInicio y horaFin correctamente.
 */
public class AsignadorHorasLocalTime {

    private final LocalTime horaInicioDia;
    private final LocalTime horaFinDia;
    private final List<Validador> validadoresHora;

    public AsignadorHorasLocalTime(LocalTime horaInicioDia,
                                   LocalTime horaFinDia,
                                   List<Validador> validadoresHora) {

        this.horaInicioDia = horaInicioDia;
        this.horaFinDia = horaFinDia;

        this.validadoresHora = 
                validadoresHora != null ? new ArrayList<>(validadoresHora)
                                        : new ArrayList<>();
    }

    public void asignarHoras(HorarioSemana semana) {
        for (HorarioDia dia : semana.getDiasSemana()) {
            asignarHorasEnDia(dia);
        }
    }

    private void asignarHorasEnDia(HorarioDia dia) {

        // Copiamos bloques del día
        List<BloqueHorario> bloques = new ArrayList<>(dia.getBloques());

        // Orden por duración descendente
        bloques.sort((a, b) -> b.getDuracion().compareTo(a.getDuracion()));

        List<BloqueHorario> asignados = new ArrayList<>();

        for (BloqueHorario bloque : bloques) {

            boolean pudo = intentarColocarBloque(dia, bloque, asignados);

            // Si no se pudo, dejamos su hora intacta y sigue sin posicionar
            if (!pudo) {
                System.out.println("Advertencia: No se pudo asignar hora al bloque " + bloque.getId());
            }
        }

        // Actualiza el día con el orden final
        dia.getBloques().clear();
        dia.getBloques().addAll(asignados);
    }

    private boolean intentarColocarBloque(HorarioDia dia,
                                          BloqueHorario bloque,
                                          List<BloqueHorario> asignados) {

        Duration dur = bloque.getDuracion();

        LocalTime tiempo = horaInicioDia;

        while (!tiempo.plus(dur).isAfter(horaFinDia)) {

            LocalTime inicio = tiempo;
            LocalTime fin = tiempo.plus(dur);

            // 1. Checar empalme básico
            if (hayEmpalmeCon(inicio, fin, asignados)) {
                tiempo = tiempo.plusMinutes(30);
                continue;
            }

            // 2. Modificamos temporalmente
            LocalTime inicioOriginal = bloque.getHoraInicio();
            LocalTime finOriginal = bloque.getHoraFin();

            bloque.actualizarIntervalo(inicio, fin);

            // 3. Validadores
            boolean valido = true;
            for (BloqueHorario other : asignados) {
                for (Validador v : validadoresHora) {
                    if (!v.esValido(bloque, other)) {
                        valido = false;
                        break;
                    }
                }
                if (!valido) break;
            }

            if (valido) {
                asignados.add(bloque);
                return true;
            }

            // Revertimos
            bloque.actualizarIntervalo(inicioOriginal, finOriginal);

            tiempo = tiempo.plusMinutes(30);
        }

        return false;
    }

    private boolean hayEmpalmeCon(LocalTime inicio,
                                  LocalTime fin,
                                  List<BloqueHorario> asignados) {

        for (BloqueHorario b : asignados) {

            LocalTime bs = b.getHoraInicio();
            LocalTime bf = b.getHoraFin();

            boolean intersecta =
                !(fin.compareTo(bs) <= 0 || bf.compareTo(inicio) <= 0);

            if (intersecta) return true;
        }

        return false;
    }
}
