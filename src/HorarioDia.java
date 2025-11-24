package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un dia del horario (lista de bloques asignados) dentro del composite.
 */
public class HorarioDia implements HorarioComponente, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String dia;
    private List<BloqueHorario> bloquesHorario;

    /**
     * Crea un dia con su etiqueta (ej. "Lunes") y sin bloques.
     */
    public HorarioDia(String dia) {
        this.dia = dia;
        this.bloquesHorario = new ArrayList<>();
    }

    /**
     * Nombre del dia.
     */
    public String getDia() {
        return dia;
    }

    /**
     * Actualiza el nombre del dia.
     */
    public void setDia(String dia) {
        this.dia = dia;
    }

    /**
     * Lista de bloques asignados al dia.
     */
    @Override
    public List<BloqueHorario> getBloques() {
        return bloquesHorario;
    }

    /**
     * Agrega un bloque al dia si no se traslapa con existentes.
     */
    @Override
    public boolean agregar(HorarioComponente comp) {
        if (!(comp instanceof BloqueHorario)) {
            throw new IllegalArgumentException("Solo se pueden agregar bloques horarios");
        }

        BloqueHorario bloque = (BloqueHorario) comp;

        if (!checkNoTraslape(bloque)) {
            return false;
        }

        bloquesHorario.removeIf(b -> b.getId().equals(bloque.getId()));
        bloque.setDia(this.dia);
        bloquesHorario.add(bloque);
        return true;
    }

    /**
     * Elimina un bloque del dia y limpia su asignacion de dia.
     */
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

    /**
     * Muestra en consola los bloques de este dia.
     */
    @Override
    public void mostrarInfo() {
        System.out.println("Horario del dia: " + dia);
        for (BloqueHorario bloque : bloquesHorario) {
            bloque.mostrarInfo();
        }
    }

    /**
     * Comprueba que un bloque no se traslapa con los ya asignados.
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
                return false;
            }
        }
        return true;
    }
}
