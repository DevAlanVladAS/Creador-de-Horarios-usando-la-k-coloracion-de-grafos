import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class HorarioSemana implements HorarioComponente {
    // Permite obtener la lista de días de la semana
    public List<HorarioDia> getDiasSemana() {
        return diasSemana;
    }
    private List<HorarioDia> diasSemana;
    private List<BloqueHorario> bloquesSinAsignar = new ArrayList<>();
    // mapa de asignaciones: blockId -> dia
    private Map<String, String> asignaciones = new HashMap<>();

    public HorarioSemana() {
        this.diasSemana = new ArrayList<HorarioDia>();
    }

    public void agregarDia(HorarioDia dia) {
        diasSemana.add(dia);
    }

    public void agregarBloqueEnDia(String dia, BloqueHorario bloque) {
        for (HorarioDia horarioDia : diasSemana) {
            if (horarioDia.getDia().equalsIgnoreCase(dia)) {
                horarioDia.agregar(bloque);
                asignaciones.put(bloque.getId(), dia);
                return;
            }
        }
        throw new IllegalArgumentException("Dia no encontrado: " + dia);
    }

    public void agregarBloqueSinAsignar(BloqueHorario bloque) {
        bloquesSinAsignar.add(bloque);
    }

    public List<BloqueHorario> getBloquesSinAsignar() {
        return new ArrayList<>(bloquesSinAsignar);
    }

    public void asignarBloquesSinAsignarADia(String dia) {
        for (BloqueHorario bloque : new ArrayList<>(bloquesSinAsignar)) {
            agregarBloqueEnDia(dia, bloque);
            bloquesSinAsignar.remove(bloque);
        }
    }

    // Buscar un bloque por id en la semana (dias + sin asignar)
    public Optional<BloqueHorario> findBlockById(String id) {
        for (HorarioDia dia : diasSemana) {
            for (BloqueHorario b : dia.getBloques()) {
                if (b.getId().equals(id)) return Optional.of(b);
            }
        }
        for (BloqueHorario b : bloquesSinAsignar) {
            if (b.getId().equals(id)) return Optional.of(b);
        }
        return Optional.empty();
    }

    // Asignar un bloque (por id) a un dia concreto. Actualiza las estructuras internas y el campo dia del bloque.
    public void assignBlockToDay(String blockId, String dia) {
        Optional<BloqueHorario> ob = findBlockById(blockId);
        if (ob.isEmpty()) throw new IllegalArgumentException("Bloque no encontrado: " + blockId);
        BloqueHorario b = ob.get();
        // si ya estaba asignado a otro dia, remover de ese dia
        String current = asignaciones.get(blockId);
        if (current != null && current.equalsIgnoreCase(dia)) return; // ya asignado
        if (current != null) {
            // quitar del dia anterior
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(current)) {
                    hd.eliminar(b);
                    break;
                }
            }
        } else {
            // si estaba en bloquesSinAsignar, remover
            bloquesSinAsignar.remove(b);
        }
        // agregar al dia destino
        agregarBloqueEnDia(dia, b);
        asignaciones.put(blockId, dia);
    }

    public void unassignBlock(String blockId) {
        Optional<BloqueHorario> ob = findBlockById(blockId);
        if (ob.isEmpty()) throw new IllegalArgumentException("Bloque no encontrado: " + blockId);
        BloqueHorario b = ob.get();
        String current = asignaciones.remove(blockId);
        if (current != null) {
            for (HorarioDia hd : diasSemana) {
                if (hd.getDia().equalsIgnoreCase(current)) {
                    hd.eliminar(b);
                    break;
                }
            }
        }
        // poner en sin asignar y desasignar el dia en el bloque
        b.setDia(null);
        if (!bloquesSinAsignar.contains(b)) bloquesSinAsignar.add(b);
    }

    public void moveBlock(String blockId, String diaDestino) {
        assignBlockToDay(blockId, diaDestino);
    }

    public Optional<String> getAssignedDay(String blockId) {
        return Optional.ofNullable(asignaciones.get(blockId));
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
        } else if (comp instanceof BloqueHorario) {
            // Los bloques van a bloquesSinAsignar hasta que se especifique el dia y de eso se encarga los algoritmos de generacion o el usuario en la interfaz
            agregarBloqueSinAsignar((BloqueHorario) comp);
        } else {
            throw new IllegalArgumentException("Tipo de componente invalido");
        }
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (comp instanceof HorarioDia) {
            diasSemana.remove((HorarioDia) comp);
        } else if (comp instanceof BloqueHorario) {
            bloquesSinAsignar.remove((BloqueHorario) comp);
        } else {
            throw new IllegalArgumentException("Tipo de componente invalido");
        }
    }

    @Override
    public List<BloqueHorario> getBloques() {
        List<BloqueHorario> todosLosBloques = new ArrayList<>();
        for (HorarioDia dia : diasSemana) {
            todosLosBloques.addAll(dia.getBloques());
        }
        todosLosBloques.addAll(bloquesSinAsignar);
        return todosLosBloques;
    }
}