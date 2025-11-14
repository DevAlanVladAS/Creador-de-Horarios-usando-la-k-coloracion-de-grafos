import java.util.ArrayList;
import java.util.List;

/**
 * Clase fachada (Facade) que centraliza las operaciones del sistema de horarios.
 * 
 * Esta clase sirve como intermediario entre la lógica de negocio y la interfaz
 * gráfica o el sistema principal. Permite manejar horarios, agregar y eliminar
 * componentes, y mostrar la información general del horario.
 * 
 * @author Aldo
 */
public class ControladorHorariosFacade {

    // Lista general de componentes de horario (profesores, grupos, aulas, etc.)
    private List<HorarioComponente> componentes;

    /**
     * Constructor que inicializa la lista de componentes.
     */
    public ControladorHorariosFacade() {
        componentes = new ArrayList<>();
    }

    /**
     * Agrega un componente al sistema de horarios.
     * 
     * @param comp Componente que implementa HorarioComponente.
     */
    public void agregarComponente(HorarioComponente comp) {
        if (comp != null) {
            componentes.add(comp);
        } else {
            System.out.println("No se puede agregar un componente nulo.");
        }
    }

    /**
     * Elimina un componente del sistema de horarios.
     * 
     * @param comp Componente a eliminar.
     */
    public void eliminarComponente(HorarioComponente comp) {
        if (componentes.remove(comp)) {
            System.out.println("Componente eliminado correctamente.");
        } else {
            System.out.println("El componente no existe o ya fue eliminado.");
        }
    }

    /**
     * Devuelve todos los bloques horarios registrados en todos los componentes.
     * 
     * @return Lista de bloques horarios.
     */
    public List<BloqueHorario> obtenerTodosLosBloques() {
        List<BloqueHorario> bloques = new ArrayList<>();
        for (HorarioComponente comp : componentes) {
            bloques.addAll(comp.getBloques());
        }
        return bloques;
    }

    /**
     * Muestra la información de todos los componentes registrados.
     */
    public void mostrarInformacionGeneral() {
        if (componentes.isEmpty()) {
            System.out.println("No hay componentes registrados en el sistema.");
            return;
        }

        System.out.println("=== Información de Horarios Registrados ===");
        for (HorarioComponente comp : componentes) {
            comp.mostrarInfo();
        }
    }

    /**
     * Busca un componente de horario por su clase (por ejemplo HorarioProfesor.class)
     * 
     * @param tipo Clase del componente buscado.
     * @return Lista de componentes del tipo indicado.
     */
    public List<HorarioComponente> buscarPorTipo(Class<?> tipo) {
        List<HorarioComponente> resultado = new ArrayList<>();
        for (HorarioComponente comp : componentes) {
            if (comp.getClass().equals(tipo)) {
                resultado.add(comp);
            }
        }
        return resultado;
    }

    /**
     * Limpia todos los componentes del sistema.
     */
    public void limpiar() {
        componentes.clear();
        System.out.println("Se eliminaron todos los componentes del sistema.");
    }
}
