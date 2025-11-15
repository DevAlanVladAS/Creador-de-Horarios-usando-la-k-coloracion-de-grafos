package src;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un día específico dentro del horario semanal.
 * Contiene una lista de bloques asignados a ese día.
 *
 * Este componente funciona dentro del patrón Composite.
 */
public class HorarioDia implements HorarioComponente {

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
    public void agregar(HorarioComponente comp) {
        if (!(comp instanceof BloqueHorario)) {
            throw new IllegalArgumentException("Solo se pueden agregar bloques horarios");
        }

        BloqueHorario bloque = (BloqueHorario) comp;


        bloquesHorario.remove(bloque);

       
        validarNoTraslape(bloque);

  
        bloque.setDia(this.dia);


        bloquesHorario.add(bloque);
    }

    @Override
    public void eliminar(HorarioComponente comp) {
        if (!(comp instanceof BloqueHorario)) {
            throw new IllegalArgumentException("Solo se pueden eliminar bloques horarios");
        }

        BloqueHorario bloque = (BloqueHorario) comp;

        boolean removed = bloquesHorario.remove(bloque);

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

 
    private void validarNoTraslape(BloqueHorario nuevo) {
        for (BloqueHorario existente : bloquesHorario) {

            if (existente == nuevo) continue;

            boolean seTraslapan =
                    !nuevo.getHoraFin().isBefore(existente.getHoraInicio()) &&
                    !nuevo.getHoraInicio().isAfter(existente.getHoraFin());

            if (seTraslapan) {
                throw new IllegalArgumentException(
                        "El bloque con ID " + nuevo.getId() +
                        " se traslapa con otro bloque ya asignado en el día " + dia
                );
            }
        }
    }
}
