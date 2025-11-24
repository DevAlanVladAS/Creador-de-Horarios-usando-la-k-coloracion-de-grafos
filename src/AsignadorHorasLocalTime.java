package src;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Asigna horas a los bloques de un horario semanal usando LocalTime, respetando
 * disponibilidad de profesores y validadores de conflicto.
 */
public class AsignadorHorasLocalTime {

    private final CatalogoRecursos catalogo;
    private final LocalTime horaInicioDia;
    private final LocalTime horaFinDia;
    private final List<Validador> validadoresHora;

    /**
     * Inicializa el asignador con el catalogo, rango de horario diario y validadores.
     */
    public AsignadorHorasLocalTime(CatalogoRecursos catalogo,
                                   LocalTime horaInicioDia,
                                   LocalTime horaFinDia,
                                   List<Validador> validadoresHora) {

        this.catalogo = catalogo;
        this.horaInicioDia = horaInicioDia;
        this.horaFinDia = horaFinDia;

        this.validadoresHora =
                validadoresHora != null ? new ArrayList<>(validadoresHora)
                                        : new ArrayList<>();
    }

    /**
     * Asigna horas a cada dia del horario semanal.
     */
    public void asignarHoras(HorarioSemana semana) {
        for (HorarioDia dia : semana.getDiasSemana()) {
            asignarHorasEnDia(dia);
        }
    }

    /**
     * Asigna horas a los bloques de un dia dado priorizando restricciones y evitando conflictos.
     */
    private void asignarHorasEnDia(HorarioDia dia) {
        System.out.println("\n  Asignando horas para " + dia.getDia() + "...");

        List<BloqueHorario> bloques = new ArrayList<>(dia.getBloques());

        if (bloques.isEmpty()) {
            System.out.println("    (sin bloques)");
            return;
        }

        bloques.sort((a, b) -> {
            boolean aRestringido = tieneRestriccionHoraria(a);
            boolean bRestringido = tieneRestriccionHoraria(b);

            if (aRestringido && !bRestringido) return -1;
            if (!aRestringido && bRestringido) return 1;

            return b.getDuracion().compareTo(a.getDuracion());
        });

        List<BloqueHorario> asignados = new ArrayList<>();
        int bloquesExitosos = 0;

        for (BloqueHorario bloque : bloques) {
            boolean pudo = intentarColocarBloque(dia, bloque, asignados, bloques);

            if (pudo) {
                bloquesExitosos++;
                System.out.println("    ✓ " + bloque.getMateria() +
                                 " (" + bloque.getHoraInicio() + "-" + bloque.getHoraFin() + ")");
            } else {
                System.out.println("    ✗ " + bloque.getMateria() +
                                 " (no se pudo asignar hora)");
                asignados.add(bloque);
            }
        }

        System.out.println("    Total: " + bloquesExitosos + "/" + bloques.size() + " bloques asignados");

        dia.getBloques().clear();
        dia.getBloques().addAll(asignados);
    }

    /**
     * Indica si el bloque tiene restricciones horarias por disponibilidad del profesor.
     */
    private boolean tieneRestriccionHoraria(BloqueHorario bloque) {
        if (bloque.getProfesorId() == null) return false;

        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) return false;

        List<String> horas = profesor.getHorasDisponibles();
        return horas != null && !horas.isEmpty();
    }

    /**
     * Intenta ubicar un bloque en un dia probando horas preferidas y luego intervalos de 50 minutos.
     */
    private boolean intentarColocarBloque(HorarioDia dia,
                                          BloqueHorario bloque,
                                          List<BloqueHorario> asignados,
                                          List<BloqueHorario> todosLosBloquesDelDia) {

        Duration dur = bloque.getDuracion();
        LocalTime tiempo = horaInicioDia;

        List<LocalTime> horasPreferidas = obtenerHorasDisponibles(bloque, dia.getDia());

        if (!horasPreferidas.isEmpty()) {
            for (LocalTime horaPreferida : horasPreferidas) {
                if (intentarAsignarEnHora(bloque, horaPreferida, dur, dia, asignados, todosLosBloquesDelDia)) {
                    return true;
                }
            }
        }

        while (!tiempo.plus(dur).isAfter(horaFinDia)) {
            if (intentarAsignarEnHora(bloque, tiempo, dur, dia, asignados, todosLosBloquesDelDia)) {
                return true;
            }
            tiempo = tiempo.plusMinutes(50);
        }

        return false;
    }

    /**
     * Intenta asignar un bloque en una hora concreta evaluando solapes y reglas.
     */
    private boolean intentarAsignarEnHora(
            BloqueHorario bloque,
            LocalTime inicio,
            Duration dur,
            HorarioDia dia,
            List<BloqueHorario> asignados,
            List<BloqueHorario> todosLosBloquesDelDia) {

        LocalTime fin = inicio.plus(dur);

        if (fin.isAfter(horaFinDia)) {
            return false;
        }

        if (hayEmpalmeCon(inicio, fin, asignados)) {
            return false;
        }

        if (!esHorarioValidoParaRecursos(bloque, dia.getDia(), inicio)) {
            return false;
        }

        LocalTime inicioOriginal = bloque.getHoraInicio();
        LocalTime finOriginal = bloque.getHoraFin();

        bloque.actualizarIntervalo(inicio, fin);

        boolean valido = true;

        if (excedeMaximoConsecutivoMateria(bloque, asignados)) {
            valido = false;
        }

        if (valido && excedeMaximoDiarioMateria(bloque, asignados, todosLosBloquesDelDia, dur)) {
            valido = false;
        }

        for (BloqueHorario other : asignados) {
            if (other == bloque) continue;

            for (Validador v : validadoresHora) {
                if (!v.validar(bloque, other, null).isEmpty()) {
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

        if (inicioOriginal != null && finOriginal != null) {
            bloque.actualizarIntervalo(inicioOriginal, finOriginal);
        } else {
            bloque.setHoraInicio(null);
            bloque.setHoraFin(null);
        }

        return false;
    }

    /**
     * Verifica si se exceden 2 horas consecutivas de la misma materia en el dia.
     */
    private boolean excedeMaximoConsecutivoMateria(BloqueHorario bloque, List<BloqueHorario> asignados) {
        if (bloque.getMateria() == null || bloque.getHoraInicio() == null || bloque.getHoraFin() == null) {
            return false;
        }

        List<BloqueHorario> mismos = new ArrayList<>();
        mismos.add(bloque);
        for (BloqueHorario b : asignados) {
            if (bloque.getMateria().equalsIgnoreCase(b.getMateria())
                    && b.getHoraInicio() != null && b.getHoraFin() != null) {
                mismos.add(b);
            }
        }

        if (mismos.isEmpty()) return false;

        mismos.sort(Comparator.comparing(BloqueHorario::getHoraInicio));
        LocalTime cadenaInicio = mismos.get(0).getHoraInicio();
        LocalTime cadenaFin = mismos.get(0).getHoraFin();

        for (int i = 1; i < mismos.size(); i++) {
            BloqueHorario actual = mismos.get(i);
            if (actual.getHoraInicio().equals(cadenaFin)) {
                cadenaFin = actual.getHoraFin();
            } else if (actual.getHoraInicio().isBefore(cadenaFin)) {
                cadenaFin = max(cadenaFin, actual.getHoraFin());
            } else {
                cadenaInicio = actual.getHoraInicio();
                cadenaFin = actual.getHoraFin();
            }

            if (Duration.between(cadenaInicio, cadenaFin).toMinutes() > 120) {
                return true;
            }
        }

        if (Duration.between(mismos.get(0).getHoraInicio(), mismos.get(0).getHoraFin()).toMinutes() > 120) {
            return true;
        }

        return false;
    }

    /**
     * Verifica que la suma diaria de una materia no supere 120 minutos aunque haya huecos.
     */
    private boolean excedeMaximoDiarioMateria(BloqueHorario bloque,
                                              List<BloqueHorario> asignados,
                                              List<BloqueHorario> todosLosBloquesDelDia,
                                              Duration durNuevo) {
        if (bloque.getMateria() == null) {
            return false;
        }

        long minutos = durNuevo != null ? durNuevo.toMinutes() : 0;

        Set<String> vistos = new HashSet<>();
        String idBloque = bloque.getId();

        for (BloqueHorario b : asignados) {
            if (b == null || b.getId() == null || b.getId().equals(idBloque)) continue;
            if (bloque.getMateria().equalsIgnoreCase(b.getMateria())
                    && b.getHoraInicio() != null && b.getHoraFin() != null) {
                if (vistos.add(b.getId())) {
                    minutos += Duration.between(b.getHoraInicio(), b.getHoraFin()).toMinutes();
                }
            }
        }

        if (todosLosBloquesDelDia != null) {
            for (BloqueHorario b : todosLosBloquesDelDia) {
                if (b == null || b.getId() == null || b.getId().equals(idBloque)) continue;
                if (bloque.getMateria().equalsIgnoreCase(b.getMateria())
                        && b.getHoraInicio() != null && b.getHoraFin() != null) {
                    if (vistos.add(b.getId())) {
                        minutos += Duration.between(b.getHoraInicio(), b.getHoraFin()).toMinutes();
                    }
                }
            }
        }

        return minutos > 120;
    }

    private LocalTime max(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    /**
     * Obtiene las horas disponibles del profesor como LocalTime para el dia indicado.
     */
    private List<LocalTime> obtenerHorasDisponibles(BloqueHorario bloque, String dia) {
        List<LocalTime> horas = new ArrayList<>();

        if (bloque.getProfesorId() == null) {
            return horas;
        }

        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) {
            return horas;
        }

        List<String> diasDisp = profesor.getDiasDisponibles();
        if (diasDisp != null && !diasDisp.isEmpty()) {
            boolean diaDisponible = diasDisp.stream()
                    .anyMatch(d -> d.equalsIgnoreCase(dia));
            if (!diaDisponible) {
                return horas;
            }
        }

        List<String> horasDisp = profesor.getHorasDisponibles();
        if (horasDisp == null || horasDisp.isEmpty()) {
            return horas;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        for (String horaStr : horasDisp) {
            try {
                LocalTime hora = LocalTime.parse(horaStr, formatter);
                if (!hora.isBefore(horaInicioDia) &&
                    !hora.plus(bloque.getDuracion()).isAfter(horaFinDia)) {
                    horas.add(hora);
                }
            } catch (Exception e) {
                System.err.println("Error parseando hora: " + horaStr);
            }
        }

        horas.sort(LocalTime::compareTo);
        return horas;
    }

    /**
     * Determina si el intervalo propuesto empalma con alguno ya asignado.
     */
    private boolean hayEmpalmeCon(LocalTime inicio,
                                  LocalTime fin,
                                  List<BloqueHorario> asignados) {

        for (BloqueHorario b : asignados) {
            if (b.getHoraInicio() == null || b.getHoraFin() == null) {
                continue;
            }

            LocalTime bs = b.getHoraInicio();
            LocalTime bf = b.getHoraFin();

            boolean intersecta =
                !(fin.compareTo(bs) <= 0 || bf.compareTo(inicio) <= 0);

            if (intersecta) return true;
        }

        return false;
    }

    /**
     * Valida que profesor (y potencialmente otros recursos) esten disponibles para ese dia/hora.
     */
    private boolean esHorarioValidoParaRecursos(BloqueHorario bloque, String dia, LocalTime hora) {
        String profesorId = bloque.getProfesorId();
        if (profesorId != null) {
            Profesor profesor = catalogo.obtenerProfesorPorId(profesorId);
            if (profesor != null) {
                List<String> diasDisponibles = profesor.getDiasDisponibles();
                if (diasDisponibles != null && !diasDisponibles.isEmpty()) {
                    boolean diaValido = diasDisponibles.stream()
                            .anyMatch(d -> d.equalsIgnoreCase(dia));
                    if (!diaValido) {
                        return false;
                    }
                }

                List<String> horasDisponibles = profesor.getHorasDisponibles();
                if (horasDisponibles != null && !horasDisponibles.isEmpty()) {
                    String horaFormateada = hora.format(DateTimeFormatter.ofPattern("H:mm"));
                    if (!horasDisponibles.contains(horaFormateada)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
