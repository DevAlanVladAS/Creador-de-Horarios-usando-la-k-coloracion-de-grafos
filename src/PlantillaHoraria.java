package src;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Define la plantilla estándar de bloques académicos que se usa en toda la aplicación.
 */
public final class PlantillaHoraria {

    private PlantillaHoraria() {}

    public static final List<LocalTime> BLOQUES_ESTANDAR = List.of(
        LocalTime.of(7, 0),
        LocalTime.of(8, 0),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        LocalTime.of(12, 0),
        LocalTime.of(13, 0),
        LocalTime.of(14, 0)
    );

    public static final Duration DURACION_BLOQUE = Duration.ofMinutes(60);
}
