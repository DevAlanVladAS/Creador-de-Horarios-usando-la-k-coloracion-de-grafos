package src;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Anima la colocacion visual y en el modelo de una lista de bloques,
 * avanzando uno a uno con un temporizador para dar feedback al usuario.
 */
public class AnimadorHorario {

    private final GestorHorarios gestor;
    private final Queue<BloqueHorario> colaDeBloques;
    private final Timer timer;
    private final JLabel estadoLabel;
    private final Runnable alFinalizar;

    /**
     * Prepara la animacion con la lista de bloques, la etiqueta de estado y un callback final.
     */
    public AnimadorHorario(List<BloqueHorario> bloquesAAsignar, JLabel estadoLabel, Runnable alFinalizar) {
        this.gestor = GestorHorarios.getInstance();
        this.colaDeBloques = new LinkedList<>(bloquesAAsignar);
        this.estadoLabel = estadoLabel;
        this.alFinalizar = alFinalizar;

        // Mezcla la cola para un efecto mas dinamico al mostrar asignaciones.
        Collections.shuffle((List<?>) this.colaDeBloques);

        this.timer = new Timer(75, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (colaDeBloques.isEmpty()) {
                    timer.stop();
                    if (estadoLabel != null) {
                        estadoLabel.setText("Estado: Animacion completada.");
                    }
                    if (alFinalizar != null) {
                        alFinalizar.run();
                    }
                    return;
                }

                BloqueHorario bloque = colaDeBloques.poll();
                if (bloque.getDia() != null && bloque.getHoraInicio() != null) {
                    // Actualiza la posicion a traves del gestor para notificar observadores.
                    gestor.actualizarPosicionBloque(bloque, bloque.getDia(), bloque.getHoraInicio());
                }

                if (estadoLabel != null) {
                    int restantes = colaDeBloques.size();
                    estadoLabel.setText("Estado: Asignando bloques... restantes: " + restantes);
                }
            }
        });
    }

    /**
     * Inicia la animacion si el temporizador no esta ya corriendo.
     */
    public void iniciar() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }
}
