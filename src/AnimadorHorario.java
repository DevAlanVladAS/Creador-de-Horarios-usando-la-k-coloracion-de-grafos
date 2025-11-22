package src;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Anima la colocación de bloques en el horario después de la generación automática.
 * Ahora funciona a través de GestorHorarios para actualizar el modelo.
 */
public class AnimadorHorario {

    private final GestorHorarios gestor;
    private final Queue<BloqueHorario> colaDeBloques;
    private final Timer timer;
    private final JLabel estadoLabel;
    private final Runnable alFinalizar;

    public AnimadorHorario(List<BloqueHorario> bloquesAAsignar, JLabel estadoLabel, Runnable alFinalizar) {
        this.gestor = GestorHorarios.getInstance();
        this.colaDeBloques = new LinkedList<>(bloquesAAsignar);
        this.estadoLabel = estadoLabel;
        this.alFinalizar = alFinalizar;

        // Mezclar para un efecto más dinámico
        Collections.shuffle((List<?>) this.colaDeBloques);

        this.timer = new Timer(75, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (colaDeBloques.isEmpty()) {
                    timer.stop();
                    if (estadoLabel != null) {
                        estadoLabel.setText("Estado: Animación completada.");
                    }
                    if (alFinalizar != null) {
                        alFinalizar.run();
                    }
                    return;
                }

                BloqueHorario bloque = colaDeBloques.poll();
                if (bloque.getDia() != null && bloque.getHoraInicio() != null) {
                    // Actualizar posición a través del gestor para notificar observers
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
     * Inicia la animación de colocación de bloques.
     */
    public void iniciar() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }
}