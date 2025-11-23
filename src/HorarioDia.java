package src;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un día específico dentro del horario semanal.
 * Contiene una lista de bloques asignados a ese día.
 *
 * Este componente funciona dentro del patrón Composite.
 */
public class HorarioDia implements HorarioComponente, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String dia;
    private List<BloqueHorario> bloquesHorario;

    public HorarioDia(String dia) {
        this.dia = dia;
        this.bloquesHorario = new ArrayList<>();
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    @Override
    public List<BloqueHorario> getBloques() {
        return bloquesHorario;
    }


    @Override
    public boolean agregar(HorarioComponente comp) {
        if (!(comp instanceof BloqueHorario)) {
            throw new IllegalArgumentException("Solo se pueden agregar bloques horarios");
        }

        BloqueHorario bloque = (BloqueHorario) comp;

        // Si hay traslape, no se puede agregar.
        if (!checkNoTraslape(bloque)) {
            return false;
        }

        // Asegurar que no queden duplicados por ID.
        bloquesHorario.removeIf(b -> b.getId().equals(bloque.getId()));
        bloque.setDia(this.dia);
        bloquesHorario.add(bloque);
        return true;
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (!(comp instanceof BloqueHorario)) {
            throw new IllegalArgumentException("Solo se pueden eliminar bloques horarios");
        }

        BloqueHorario bloque = (BloqueHorario) comp;
        boolean removed = bloquesHorario.removeIf(b -> b.getId().equals(bloque.getId()));

        if (removed) {
            bloque.setDia(null);
        }
    }

    @Override
    public void mostrarInfo() {
        System.out.println("Horario del día: " + dia);
        for (BloqueHorario bloque : bloquesHorario) {
            bloque.mostrarInfo();
        }
    }

    /**
     * Comprueba si un nuevo bloque se traslapa con los existentes.
     * @param nuevo El bloque a comprobar.
     * @return `true` si no hay traslape, `false` si lo hay.
     */
    private boolean checkNoTraslape(BloqueHorario nuevo) {
        for (BloqueHorario existente : bloquesHorario) {
            if (nuevo.getHoraInicio() == null || existente.getHoraInicio() == null) {
                continue;
            }

            if (existente.getId().equals(nuevo.getId())) continue;

            boolean seTraslapan =
                    nuevo.getHoraInicio().isBefore(existente.getHoraFin()) &&
                    nuevo.getHoraFin().isAfter(existente.getHoraInicio());

            if (seTraslapan) {
                return false; // Hay traslape
            }
        }
        return true; // No hay traslape
    }
}
