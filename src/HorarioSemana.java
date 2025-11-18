package src;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Representa un conjunto de días de trabajo de la semana
 * y los bloques horarios asignados o sin asignar.
 *
 * Esta clase funciona como "Composite" dentro del patrón Composite.
 */
public class HorarioSemana implements HorarioComponente, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<HorarioDia> diasSemana;                     // Días (Lunes, Martes, ...)
    private List<BloqueHorario> bloquesSinAsignar;           // Bloques aún no colocados
    private Map<String, String> asignaciones;                // Mapa: ID bloque → día asignado

    public HorarioSemana() {
        this.diasSemana = new ArrayList<>();
        this.bloquesSinAsignar = new ArrayList<>();
        this.asignaciones = new HashMap<>();
    }


    public List<HorarioDia> getDiasSemana() {
        return diasSemana;
    }

    public List<BloqueHorario> getBloquesSinAsignar() {
        return new ArrayList<>(bloquesSinAsignar);
    }

    public Optional<String> getDiaAsignado(String idBloque) {
        return Optional.ofNullable(asignaciones.get(idBloque));
    }

    public void agregarDia(HorarioDia dia) {
        diasSemana.add(dia);
    }

    public void agregarBloqueEnDia(String dia, BloqueHorario bloque) {
        for (HorarioDia horarioDia : diasSemana) {
            if (horarioDia.getDia().equalsIgnoreCase(dia)) {
                
                horarioDia.eliminar(bloque);

                horarioDia.agregar(bloque);

                bloque.setDia(dia);

                asignaciones.put(bloque.getId(), dia);
                return;
            }
        }
        throw new IllegalArgumentException("Día no encontrado: " + dia);
    }

    public void agregarBloqueSinAsignar(BloqueHorario bloque) {
        if (!bloquesSinAsignar.contains(bloque)) {
            bloquesSinAsignar.add(bloque);
        }
        bloque.setDia(null);
        asignaciones.remove(bloque.getId());
    }

    public void asignarBloqueADia(String idBloque, String dia) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            throw new IllegalArgumentException("Bloque no encontrado: " + idBloque);
        }

        BloqueHorario bloque = contenedor.get();
        String diaActual = asignaciones.get(idBloque);

        if (diaActual != null && diaActual.equalsIgnoreCase(dia)) {
            return;
        }

        if (diaActual != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(diaActual)) {
                    hd.eliminar(bloque);
                    break;
                }
            }
        } else {
           
            bloquesSinAsignar.remove(bloque);
        }

        agregarBloqueEnDia(dia, bloque);

        bloque.setDia(dia);
        asignaciones.put(idBloque, dia);
    }

    public void desasignarBloqueADia(String idBloque) {
        Optional<BloqueHorario> contenedor = obtenerBloquePorID(idBloque);
        if (contenedor.isEmpty()) {
            throw new IllegalArgumentException("Bloque no encontrado: " + idBloque);
        }

        BloqueHorario bloque = contenedor.get();
        String diaActual = asignaciones.remove(idBloque);

        if (diaActual != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(diaActual)) {
                    hd.eliminar(bloque);
                    break;
                }
            }
        }

        bloque.setDia(null);

        if (!bloquesSinAsignar.contains(bloque)) {
            bloquesSinAsignar.add(bloque);
        }
    }

    public void moverBloque(String idBloque, String diaDestino) {
        asignarBloqueADia(idBloque, diaDestino);
    }


    public Optional<BloqueHorario> obtenerBloquePorID(String id) {
        for (HorarioDia dia : diasSemana) {
            for (BloqueHorario b : dia.getBloques()) {
                if (b.getId().equals(id)) {
                    return Optional.of(b);
                }
            }
        }

        for (BloqueHorario b : bloquesSinAsignar) {
            if (b.getId().equals(id)) {
                return Optional.of(b);
            }
        }

        return Optional.empty();
    }

    @Override
    public void mostrarInfo() {
        for (HorarioDia dia : diasSemana) {
            dia.mostrarInfo();
        }
        if (!bloquesSinAsignar.isEmpty()) {
            System.out.println("Bloques sin asignar: " + bloquesSinAsignar.size());
        }
    }

    @Override
    public void agregar(HorarioComponente comp) {
        if (comp instanceof HorarioDia) {
            diasSemana.add((HorarioDia) comp);
            return;
        }
        if (comp instanceof BloqueHorario) {
            agregarBloqueSinAsignar((BloqueHorario) comp);
            return;
        }
        throw new IllegalArgumentException("Tipo inválido: " + comp);
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (comp instanceof HorarioDia) {
            diasSemana.remove((HorarioDia) comp);
            return;
        }
        if (comp instanceof BloqueHorario) {
            bloquesSinAsignar.remove((BloqueHorario) comp);
            return;
        }
        throw new IllegalArgumentException("Tipo inválido: " + comp);
    }

    @Override
    public List<BloqueHorario> getBloques() {
        List<BloqueHorario> todos = new ArrayList<>();
        for (HorarioDia dia : diasSemana) {
            todos.addAll(dia.getBloques());
        }
        todos.addAll(bloquesSinAsignar);
        return todos;
    }
}
