/**
 * Controlador encargado de manejar la generación de gráficas de horarios.
 */
public class ControladorDeVisualizacion {

    private AdaptadorGraficaDeHorarios adaptadorGrafica;

    public ControladorDeVisualizacion(AdaptadorGraficaDeHorarios adaptador) {
        this.adaptadorGrafica = adaptador;
    }

    public void generarGrafica(HorarioSemana horario) {
        // Genera la representación visual del horario usando el adaptador
    }
}
