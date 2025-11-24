package src;

import java.util.List;

/**
 * Componente del patrón Composite para representar estructuras de horario.
 * Puede ser una hoja (BloqueHorario) o un contenedor (HorarioDia/HorarioSemana).
 */
public interface HorarioComponente {

    /**
     * Agrega un subcomponente; en hojas lanza UnsupportedOperationException.
     */
    boolean agregar(HorarioComponente comp);

    /**
     * Elimina un subcomponente; en hojas lanza UnsupportedOperationException.
     */
    void eliminar(HorarioComponente comp);

    /**
     * Devuelve todos los bloques contenidos (o este mismo si es hoja).
     */
    List<BloqueHorario> getBloques();
    
    /**
     * Muestra información del componente (para depuración/visualización).
     */
    void mostrarInfo();
}
