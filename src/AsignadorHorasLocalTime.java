package src;
import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

/**
 * Fase 3: Asignación de horas dentro de un día usando LocalTime.
 * CORREGIDO: Mejor manejo de disponibilidad y conflictos.
 * 
 * - Ordena bloques por duración (más largos primero).
 * - Intenta colocarlos empezando desde horaInicioDia en intervalos de 50 min.
 * - Respeta validadores de conflicto (recurso, profesor, grupo, etc.)
 * - Actualiza horaInicio y horaFin correctamente.
 */
public class AsignadorHorasLocalTime {

    private final CatalogoRecursos catalogo;
    private final LocalTime horaInicioDia;
    private final LocalTime horaFinDia;
    private final List<Validador> validadoresHora;

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

    public void asignarHoras(HorarioSemana semana) {
        for (HorarioDia dia : semana.getDiasSemana()) {
            asignarHorasEnDia(dia);
        }
    }

    private void asignarHorasEnDia(HorarioDia dia) {
        System.out.println("\n  Asignando horas para " + dia.getDia() + "...");
        
        // Copiamos bloques del día
        List<BloqueHorario> bloques = new ArrayList<>(dia.getBloques());
        
        if (bloques.isEmpty()) {
            System.out.println("    (sin bloques)");
            return;
        }

        // MEJORADO: Orden por prioridad
        // 1. Bloques con restricciones de hora primero
        // 2. Luego por duración descendente
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
                // CORREGIDO: Mantener en la lista pero sin hora
                asignados.add(bloque);
            }
        }

        System.out.println("    Total: " + bloquesExitosos + "/" + bloques.size() + " bloques asignados");

        // Actualiza el día con el orden final
        dia.getBloques().clear();
        dia.getBloques().addAll(asignados);
    }

    /**
     * NUEVO: Verifica si un bloque tiene restricciones horarias del profesor
     */
    private boolean tieneRestriccionHoraria(BloqueHorario bloque) {
        if (bloque.getProfesorId() == null) return false;
        
        Profesor profesor = catalogo.obtenerProfesorPorId(bloque.getProfesorId());
        if (profesor == null) return false;
        
        List<String> horas = profesor.getHorasDisponibles();
        return horas != null && !horas.isEmpty();
    }

    private boolean intentarColocarBloque(HorarioDia dia,
                                          BloqueHorario bloque,
                                          List<BloqueHorario> asignados,
                                          List<BloqueHorario> todosLosBloquesDelDia) {

        Duration dur = bloque.getDuracion();
        LocalTime tiempo = horaInicioDia;
        
        // MEJORADO: Obtener horas disponibles del profesor si existen
        List<LocalTime> horasPreferidas = obtenerHorasDisponibles(bloque, dia.getDia());

        // ESTRATEGIA 1: Intentar en horas preferidas del profesor
        if (!horasPreferidas.isEmpty()) {
            for (LocalTime horaPreferida : horasPreferidas) {
                if (intentarAsignarEnHora(bloque, horaPreferida, dur, dia, asignados, todosLosBloquesDelDia)) {
                    return true;
                }
            }
        }

        // ESTRATEGIA 2: Búsqueda exhaustiva en intervalos de 50 minutos
        while (!tiempo.plus(dur).isAfter(horaFinDia)) {
            if (intentarAsignarEnHora(bloque, tiempo, dur, dia, asignados, todosLosBloquesDelDia)) {
                return true;
            }
            tiempo = tiempo.plusMinutes(50); // CORREGIDO: Bloques de 50 min
        }

        return false;
    }

    /**
     * NUEVO: Intenta asignar un bloque en una hora específica
     */
    private boolean intentarAsignarEnHora(
            BloqueHorario bloque,
            LocalTime inicio,
            Duration dur,
            HorarioDia dia,
            List<BloqueHorario> asignados,
            List<BloqueHorario> todosLosBloquesDelDia) {
        
        LocalTime fin = inicio.plus(dur);
        
        // Verificar que no exceda el horario del día
        if (fin.isAfter(horaFinDia)) {
            return false;
        }

        // 1. Checar empalme básico
        if (hayEmpalmeCon(inicio, fin, asignados)) {
            return false;
        }

        // 2. Checar disponibilidad de recursos (profesor, etc.)
        if (!esHorarioValidoParaRecursos(bloque, dia.getDia(), inicio)) {
            return false;
        }

        // 3. Guardar estado original
        LocalTime inicioOriginal = bloque.getHoraInicio();
        LocalTime finOriginal = bloque.getHoraFin();

        bloque.actualizarIntervalo(inicio, fin);

        // 4. Validadores
        boolean valido = true;

        // Regla: no más de 2 horas seguidas de la misma materia en el día
        if (excedeMaximoConsecutivoMateria(bloque, asignados)) {
            valido = false;
        }

        // Regla adicional: máximo 2 horas totales al día por materia (no dividir en dos tramos separados)
        if (valido && excedeMaximoDiarioMateria(bloque, asignados, todosLosBloquesDelDia, dur)) {
            valido = false;
        }

        for (BloqueHorario other : asignados) {
            if (other == bloque) continue;
            
            for (Validador v : validadoresHora) {
                if (!v.validar(bloque, other, null).isEmpty()) { // Asumimos que para hora no se necesita el contexto completo
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

        // Revertir
        if (inicioOriginal != null && finOriginal != null) {
            bloque.actualizarIntervalo(inicioOriginal, finOriginal);
        } else {
            bloque.setHoraInicio(null);
            bloque.setHoraFin(null);
        }

        return false;
    }

    /**
     * Verifica si al colocar este bloque se exceden 2 horas consecutivas de la misma materia en el día.
     */
    private boolean excedeMaximoConsecutivoMateria(BloqueHorario bloque, List<BloqueHorario> asignados) {
        if (bloque.getMateria() == null || bloque.getHoraInicio() == null || bloque.getHoraFin() == null) {
            return false;
        }

        // Reunir todos los intervalos de la misma materia (incluyendo el bloque actual)
        List<BloqueHorario> mismos = new ArrayList<>();
        mismos.add(bloque);
        for (BloqueHorario b : asignados) {
            if (bloque.getMateria().equalsIgnoreCase(b.getMateria())
                    && b.getHoraInicio() != null && b.getHoraFin() != null) {
                mismos.add(b);
            }
        }

        if (mismos.isEmpty()) return false;

        // Ordenar por inicio y encontrar cadenas consecutivas
        mismos.sort(Comparator.comparing(BloqueHorario::getHoraInicio));
        LocalTime cadenaInicio = mismos.get(0).getHoraInicio();
        LocalTime cadenaFin = mismos.get(0).getHoraFin();

        for (int i = 1; i < mismos.size(); i++) {
            BloqueHorario actual = mismos.get(i);
            if (actual.getHoraInicio().equals(cadenaFin)) {
                // continuidad exacta
                cadenaFin = actual.getHoraFin();
            } else if (actual.getHoraInicio().isBefore(cadenaFin)) {
                // solape — ya sería conflicto por validador de hora
                cadenaFin = max(cadenaFin, actual.getHoraFin());
            } else {
                // nueva cadena
                cadenaInicio = actual.getHoraInicio();
                cadenaFin = actual.getHoraFin();
            }

            if (Duration.between(cadenaInicio, cadenaFin).toMinutes() > 120) {
                return true;
            }
        }

        // También evaluar la primera cadena sola
        if (Duration.between(mismos.get(0).getHoraInicio(), mismos.get(0).getHoraFin()).toMinutes() > 120) {
            return true;
        }

        return false;
    }

    /**
     * Verifica que la suma total de horas de la materia en el día no supere 120 minutos,
     * incluso si estuvieran separadas por huecos.
     */
    private boolean excedeMaximoDiarioMateria(BloqueHorario bloque,
                                              List<BloqueHorario> asignados,
                                              List<BloqueHorario> todosLosBloquesDelDia,
                                              Duration durNuevo) {
        if (bloque.getMateria() == null) {
            return false;
        }

        long minutos = durNuevo != null ? durNuevo.toMinutes() : 0;

        // Usar conjunto para evitar contar el mismo bloque dos veces
        java.util.Set<String> vistos = new java.util.HashSet<>();
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
        // incluir otros bloques del día ya existentes aunque no estén en asignados, excepto el mismo bloque
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
     * NUEVO: Obtiene las horas disponibles del profesor como LocalTime
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
        
        // Verificar que el profesor esté disponible este día
        List<String> diasDisp = profesor.getDiasDisponibles();
        if (diasDisp != null && !diasDisp.isEmpty()) {
            boolean diaDisponible = diasDisp.stream()
                    .anyMatch(d -> d.equalsIgnoreCase(dia));
            if (!diaDisponible) {
                return horas; // No está disponible este día
            }
        }
        
        List<String> horasDisp = profesor.getHorasDisponibles();
        if (horasDisp == null || horasDisp.isEmpty()) {
            return horas;
        }
        
        // Convertir strings a LocalTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        for (String horaStr : horasDisp) {
            try {
                LocalTime hora = LocalTime.parse(horaStr, formatter);
                // Solo agregar si está dentro del rango del día
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

    private boolean esHorarioValidoParaRecursos(BloqueHorario bloque, String dia, LocalTime hora) {
        // Validar Profesor
        String profesorId = bloque.getProfesorId();
        if (profesorId != null) {
            Profesor profesor = catalogo.obtenerProfesorPorId(profesorId);
            if (profesor != null) {
                // Validar día
                List<String> diasDisponibles = profesor.getDiasDisponibles();
                if (diasDisponibles != null && !diasDisponibles.isEmpty()) {
                    boolean diaValido = diasDisponibles.stream()
                            .anyMatch(d -> d.equalsIgnoreCase(dia));
                    if (!diaValido) {
                        return false;
                    }
                }
                
                // Validar hora
                List<String> horasDisponibles = profesor.getHorasDisponibles();
                if (horasDisponibles != null && !horasDisponibles.isEmpty()) {
                    String horaFormateada = hora.format(DateTimeFormatter.ofPattern("H:mm"));
                    if (!horasDisponibles.contains(horaFormateada)) {
                        return false;
                    }
                }
            }
        }

        // Aquí se podrían agregar validaciones para salones, etc.

        return true;
    }
}
