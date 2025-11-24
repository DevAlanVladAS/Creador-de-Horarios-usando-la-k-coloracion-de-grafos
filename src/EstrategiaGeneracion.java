package src;

/**
 * Estrategia para generar un HorarioSemana a partir de una grafica de bloques.
 */
public interface EstrategiaGeneracion {
    /**
     * Ejecuta la generacion de horario tomando la grafica de conflictos como base.
     */
    HorarioSemana generarHorario(AdaptadorGraficaDeHorarios horarioGrafica);
}
